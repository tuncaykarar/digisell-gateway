package com.lilaksoft.digisellgateway;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import org.apache.http.protocol.HTTP;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtOffloadFilterFactory
    extends AbstractGatewayFilterFactory<JwtOffloadFilterFactory.Config> {

  private static final String HEADER_STRING = "Authorization";
  private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

  public JwtOffloadFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    //Custom Pre Filter. Suppose we can extract JWT and perform Authentication
    //if (config.isPreLogger()) {
    //  logger.info("Pre GatewayFilter logging: "
    //      + config.getBaseMessage());
    //}
    return (exchange, chain) -> {
      System.out.println("First pre filter" + exchange.getRequest());
      //Custom Post Filter.Suppose we can call error response handler based on error code.
      String token = exchange.getRequest().getHeaders().getFirst(HEADER_STRING);
      if (Optional.ofNullable(token).isEmpty() || token.isEmpty()) {
        return this.onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
      }
      //call authentication micro service to check the validity of JWT token
      HttpRequest authRequest = HttpRequest.newBuilder(URI.create("http://localhost:8081/users/userref"))
          .header(HEADER_STRING, token)
            .GET().build();
      HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
      String userRef = "";

      try {
        HttpResponse<String> response = client.send(authRequest, bodyHandler);
        if(response.statusCode() != HttpStatus.OK.value()) {
          return this.onError(exchange, "Token invalid", HttpStatus.UNAUTHORIZED);
        }
        userRef = response.body();

        if(userRef.isEmpty()){
          return this.onError(exchange, "user reference couldn't been retrieved", HttpStatus.UNAUTHORIZED);
        }

      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
      ServerHttpRequest request = exchange.getRequest()
          .mutate()
          .header("userRef", userRef)
          .build();
      ServerWebExchange intervenedExchange = exchange.mutate().request(request).build();

      return chain.filter(intervenedExchange).then(Mono.fromRunnable(() -> {
        System.out.println("First post filter");
      }));
    };
  }

  private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(httpStatus);

    return response.setComplete();
  }

  public static class Config {
    private String baseMessage;
    private boolean preLogger;
    private boolean postLogger;
  }
}
