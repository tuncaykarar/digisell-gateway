package com.lilaksoft.digisellgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
@RestController
public class DigisellGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(DigisellGatewayApplication.class, args);
  }

  @Bean
  public RouteLocator myRoutes(RouteLocatorBuilder builder,
                               JwtOffloadFilterFactory jwtOffloadFilterFactory,
                               UriConfiguration uriConfiguration) {
    String httpUri = uriConfiguration.getHttpbin();
    String coreApiUtl = uriConfiguration.getCoreApiUrl();
    return builder.routes()
        .route(p -> p
            .path("/get")
            .filters(f -> f.addRequestHeader("Hello", "World"))
            .uri(httpUri))
        //.route("api", r -> r.path("/api")
        //		.filters(f -> f.filter(new JwtOffloadCustomFilter()))
        //		.uri(coreApiUtl))
        .route("api", r -> r.path("/api/**")
            .filters(f -> f.filter(jwtOffloadFilterFactory.apply(new JwtOffloadFilterFactory.Config())))
            .uri(coreApiUtl))
        .route("authentication", r -> r.path("/users")
            .filters(f -> f.addRequestHeader("Hello", "World"))
            .uri(coreApiUtl))
        .route(p -> p
            .host("*.hystrix.com")
            .filters(f -> f
                .hystrix(config -> config
                    .setName("mycmd")
                    .setFallbackUri("forward:/fallback")))
            .uri(httpUri))
        //.route(r -> r.path("/api").filters(f -> ))
        .build();
  }

  @RequestMapping("/fallback")
  public Mono<String> fallback() {
    return Mono.just("fallback");
  }

}
