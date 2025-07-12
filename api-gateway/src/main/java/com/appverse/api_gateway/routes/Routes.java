
package com.appverse.api_gateway.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Configuration
public class Routes {
    private static final Logger logger = LoggerFactory.getLogger(Routes.class);



    private final URI fallbackUri = URI.create("forward:/fallback"); 

    @Bean
    public RouterFunction<ServerResponse> developerServiceApiRoute() {
        return GatewayRouterFunctions.route("developer-service-api")
                .route(RequestPredicates.path("/api/developers/**"), HandlerFunctions.http("http://localhost:8081"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("developerServiceCB", fallbackUri)) 
                .filter((request, next) -> {
                    logger.info("[API] Forwarding request for {} to developer-service (http://localhost:8081)",
                            request.uri());
                    return next.handle(request);
                })
                
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> applicationServiceApiRoute() {
        return GatewayRouterFunctions.route("application-service-api")
                .route(RequestPredicates.path("/api/apps/**"), HandlerFunctions.http("http://localhost:8080"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("applicationServiceCB", fallbackUri)) 
                .filter((request, next) -> {
                    logger.info("[API] Forwarding request for {} to application-service (http://localhost:8080)",
                            request.uri());
                    return next.handle(request);
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> categoryServiceApiRoute() {
        return GatewayRouterFunctions.route("category-service-api")
                .route(RequestPredicates.path("/api/categories/**"), HandlerFunctions.http("http://localhost:8080"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("applicationServiceCB", fallbackUri))  
                .filter((request, next) -> {
                    logger.info(
                            "[API] Forwarding request for {} to category-service (via app-service http://localhost:8080)",
                            request.uri());
                    return next.handle(request);
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceApiRoute() {
        return GatewayRouterFunctions.route("user-service-api")
                .route(RequestPredicates.path("/api/v1/users/**"), HandlerFunctions.http("http://localhost:8082"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("userServiceCB", fallbackUri)) 
                .filter((request, next) -> {
                    logger.info("[API] Forwarding request for {} to user-service (http://localhost:8082)",
                            request.uri());
                    return next.handle(request);
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> cartServiceApiRoute() {
        return GatewayRouterFunctions.route("cart-service-api")
                .route(RequestPredicates.path("/api/v1/carts/**"), HandlerFunctions.http("http://localhost:8083"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("cartServiceCB", fallbackUri)) 
                .filter((request, next) -> {
                    logger.info("[API] Forwarding request for {} to cart-service (http://localhost:8083)",
                            request.uri());
                    return next.handle(request);
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceApiRoute() {
        return GatewayRouterFunctions.route("order-service-api")
                .route(RequestPredicates.path("/api/v1/orders/**"), HandlerFunctions.http("http://localhost:8084"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceCB", fallbackUri)) 
                .filter((request, next) -> {
                    logger.info("[API] Forwarding request for {} to order-service (http://localhost:8084)",
                            request.uri());
                    return next.handle(request);
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> paymentServiceApiRoute() {
        return GatewayRouterFunctions.route("payment-service-api")
                .route(RequestPredicates.path("/api/v1/payments/**"), HandlerFunctions.http("http://localhost:8085"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("paymentServiceCB", fallbackUri)) 
                .filter((request, next) -> {
                    logger.info("[API] Forwarding request for {} to payment-service (http://localhost:8085)",
                            request.uri());
                    return next.handle(request);
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> subscriptionServiceApiRoute() {
        return GatewayRouterFunctions.route("subscription-service-api")
                .route(RequestPredicates.path("/api/v1/subscriptions/**"),
                        HandlerFunctions.http("http://localhost:8086"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("subscriptionServiceCB", fallbackUri)) 
                .filter((request, next) -> {
                    logger.info("[API] Forwarding request for {} to subscription-service (http://localhost:8086)",
                            request.uri());
                    return next.handle(request);
                })
                .build();
    }
    @Bean
    public RouterFunction<ServerResponse> subscriptionPlanServiceApiRoute() {
        return GatewayRouterFunctions.route("subscription-service-api")
                .route(RequestPredicates.path("/api/v1/subscription-plans/**"),
                        HandlerFunctions.http("http://localhost:8086"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("subscriptionServiceCB", fallbackUri)) 
                .filter((request, next) -> {
                    logger.info("[API] Forwarding request for {} to subscription-service (http://localhost:8086)",
                            request.uri());
                    return next.handle(request);
                })
                .build();
    }

   
    private ServerRequest rewriteToApiDocs(ServerRequest request) {
        URI newUri = UriComponentsBuilder.fromUri(request.uri())
                .replacePath("/v3/api-docs")
                .build(true)
                .toUri();
        return ServerRequest.from(request).uri(newUri).build();
    }

    @Bean
    public RouterFunction<ServerResponse> applicationServiceApiDocsRoute() {
        return GatewayRouterFunctions.route("application-service-apidocs")
                .route(RequestPredicates.path("/aggregate/application-service/v3/api-docs")
                        .and(RequestPredicates.method(HttpMethod.GET)),
                        HandlerFunctions.http("http://localhost:8080"))
                .filter((request, next) -> {
                    ServerRequest modifiedRequest = rewriteToApiDocs(request);
                    logger.info("[APIDOCS] Forwarding {} to application-service (http://localhost:8080) as {}",
                            request.uri(), modifiedRequest.uri());
                    return next.handle(modifiedRequest);
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> developerServiceApiDocsRoute() {
        return GatewayRouterFunctions.route("developer-service-apidocs")
                .route(RequestPredicates.path("/aggregate/developer-service/v3/api-docs")
                        .and(RequestPredicates.method(HttpMethod.GET)),
                        HandlerFunctions.http("http://localhost:8081"))
                .filter((request, next) -> {
                    ServerRequest modifiedRequest = rewriteToApiDocs(request);
                    logger.info("[APIDOCS] Forwarding {} to developer-service (http://localhost:8081) as {}",
                            request.uri(), modifiedRequest.uri());
                    return next.handle(modifiedRequest);
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceApiDocsRoute() {
        return GatewayRouterFunctions.route("user-service-apidocs")
                .route(RequestPredicates.path("/aggregate/user-service/v3/api-docs")
                        .and(RequestPredicates.method(HttpMethod.GET)),
                        HandlerFunctions.http("http://localhost:8082"))
                .filter((request, next) -> {
                    ServerRequest modifiedRequest = rewriteToApiDocs(request);
                    logger.info("[APIDOCS] Forwarding {} to user-service (http://localhost:8082) as {}", request.uri(),
                            modifiedRequest.uri());
                    return next.handle(modifiedRequest);
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> cartServiceApiDocsRoute() {
        return GatewayRouterFunctions.route("cart-service-apidocs")
                .route(RequestPredicates.path("/aggregate/cart-service/v3/api-docs")
                        .and(RequestPredicates.method(HttpMethod.GET)),
                        HandlerFunctions.http("http://localhost:8083"))
                .filter((request, next) -> {
                    ServerRequest modifiedRequest = rewriteToApiDocs(request);
                    logger.info("[APIDOCS] Forwarding {} to cart-service (http://localhost:8083) as {}", request.uri(),
                            modifiedRequest.uri());
                    return next.handle(modifiedRequest);
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceApiDocsRoute() {
        return GatewayRouterFunctions.route("order-service-apidocs")
                .route(RequestPredicates.path("/aggregate/order-service/v3/api-docs")
                        .and(RequestPredicates.method(HttpMethod.GET)),
                        HandlerFunctions.http("http://localhost:8084"))
                .filter((request, next) -> {
                    ServerRequest modifiedRequest = rewriteToApiDocs(request);
                    logger.info("[APIDOCS] Forwarding {} to order-service (http://localhost:8084) as {}", request.uri(),
                            modifiedRequest.uri());
                    return next.handle(modifiedRequest);
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> paymentServiceApiDocsRoute() {
        return GatewayRouterFunctions.route("payment-service-apidocs")
                .route(RequestPredicates.path("/aggregate/payment-service/v3/api-docs")
                        .and(RequestPredicates.method(HttpMethod.GET)),
                        HandlerFunctions.http("http://localhost:8085"))
                .filter((request, next) -> {
                    ServerRequest modifiedRequest = rewriteToApiDocs(request);
                    logger.info("[APIDOCS] Forwarding {} to payment-service (http://localhost:8085) as {}",
                            request.uri(), modifiedRequest.uri());
                    return next.handle(modifiedRequest);
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> subscriptionServiceApiDocsRoute() {
        return GatewayRouterFunctions.route("subscription-service-apidocs")
                .route(RequestPredicates.path("/aggregate/subscription-service/v3/api-docs")
                        .and(RequestPredicates.method(HttpMethod.GET)),
                        HandlerFunctions.http("http://localhost:8086"))
                .filter((request, next) -> {
                    ServerRequest modifiedRequest = rewriteToApiDocs(request);
                    logger.info("[APIDOCS] Forwarding {} to subscription-service (http://localhost:8086) as {}",
                            request.uri(), modifiedRequest.uri());
                    return next.handle(modifiedRequest);
                })
                .build();
    }

     @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return GatewayRouterFunctions.route("fallbackRoute")
                .GET("/fallback",
                        request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body("Service Unavailable. Please try again later."))
                .build();
    }
}