package com.lilaksoft.digisellgateway;

import java.util.Optional;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtOffloadFilterFactory
    extends AbstractGatewayFilterFactory<JwtOffloadFilterFactory.Config> {

  private static final String HEADER_STRING = "Authorization";

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
      boolean authorized = false;
      //TODO call authentication micro service to check the validity of JWT token

      if (!authorized) {
        return this.onError(exchange, "Token invalid", HttpStatus.UNAUTHORIZED);
      }
      return chain.filter(exchange).then(Mono.fromRunnable(() -> {
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
