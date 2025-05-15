package com.appverse.api_gateway.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
// Make sure ServerRequest is imported if you use 'request.uri()' or other ServerRequest methods
// import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class Routes {
    private static final Logger logger = LoggerFactory.getLogger(Routes.class);

    @Bean
    public RouterFunction<ServerResponse> developerServiceRoute() {
        return GatewayRouterFunctions.route("developer-service")
                .route(RequestPredicates.path("/api/developers/**"), HandlerFunctions.http("http://localhost:8081"))
                // Optional: Add a filter here too if you want to log developer-service requests
                .filter((request, next) -> {
                    logger.info("Forwarding request for {} to developer-service (http://localhost:8081)", request.uri());
                    return next.handle(request); // Ensure next.handle is called and returned
                })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> applicationServiceRoute() {
        return GatewayRouterFunctions.route("application-service")
                .route(RequestPredicates.path("/api/apps/**"), HandlerFunctions.http("http://localhost:8080"))
                .filter((request, next) -> {
                    // Log the request URI before forwarding
                    logger.info("Forwarding request for {} to application-service (http://localhost:8080)", request.uri());
                    // CRITICAL: You must call next.handle(request) and return its result
                    // for the request to be forwarded.
                    return next.handle(request);
                })
                .build(); // The .build() call was misplaced inside the filter lambda.
    }

     @Bean
    public RouterFunction<ServerResponse> categoryServiceRoute() {
        return GatewayRouterFunctions.route("application-service")
                .route(RequestPredicates.path("/api/categories/**"), HandlerFunctions.http("http://localhost:8080"))
                .filter((request, next) -> {
                    // Log the request URI before forwarding
                    logger.info("Forwarding request for {} to category-service (http://localhost:8080)", request.uri());
                    // CRITICAL: You must call next.handle(request) and return its result
                    // for the request to be forwarded.
                    return next.handle(request);
                })
                .build(); // The .build() call was misplaced inside the filter lambda.
    }

     @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return GatewayRouterFunctions.route("user-service")
                .route(RequestPredicates.path("/api/v1/users/**"), HandlerFunctions.http("http://localhost:8082"))
                .filter((request, next) -> {
                    // Log the request URI before forwarding
                    logger.info("Forwarding request for {} to user-service (http://localhost:8082)", request.uri());
                    // CRITICAL: You must call next.handle(request) and return its result
                    // for the request to be forwarded.
                    return next.handle(request);
                })
                .build(); // The .build() call was misplaced inside the filter lambda.
    }

     @Bean
    public RouterFunction<ServerResponse> cartServiceRoute() {
        return GatewayRouterFunctions.route("user-service")
                .route(RequestPredicates.path("/api/v1/carts/**"), HandlerFunctions.http("http://localhost:8083"))
                .filter((request, next) -> {
                    // Log the request URI before forwarding
                    logger.info("Forwarding request for {} to cart-service (http://localhost:8083)", request.uri());
                    // CRITICAL: You must call next.handle(request) and return its result
                    // for the request to be forwarded.
                    return next.handle(request);
                })
                .build(); // The .build() call was misplaced inside the filter lambda.
    }
        @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return GatewayRouterFunctions.route("order-service")
                .route(RequestPredicates.path("/api/v1/orders/**"), HandlerFunctions.http("http://localhost:8084"))
                .filter((request, next) -> {
                    // Log the request URI before forwarding
                    logger.info("Forwarding request for {} to order-service (http://localhost:8084)", request.uri());
                    // CRITICAL: You must call next.handle(request) and return its result
                    // for the request to be forwarded.
                    return next.handle(request);
                })
                .build(); // The .build() call was misplaced inside the filter lambda.
    }

        @Bean
    public RouterFunction<ServerResponse> paymentServiceRoute() {
        return GatewayRouterFunctions.route("payment-service")
                .route(RequestPredicates.path("/api/v1/payments/**"), HandlerFunctions.http("http://localhost:8085"))
                .filter((request, next) -> {
                    // Log the request URI before forwarding
                    logger.info("Forwarding request for {} to payment-service (http://localhost:8085)", request.uri());
                    // CRITICAL: You must call next.handle(request) and return its result
                    // for the request to be forwarded.
                    return next.handle(request);
                })
                .build(); // The .build() call was misplaced inside the filter lambda.
    }

            @Bean
    public RouterFunction<ServerResponse> subscriptionServiceRoute() {
        return GatewayRouterFunctions.route("subscription-service")
                .route(RequestPredicates.path("/api/v1/subscriptions/**"), HandlerFunctions.http("http://localhost:8086"))
                .filter((request, next) -> {
                    // Log the request URI before forwarding
                    logger.info("Forwarding request for {} to subscription-service (http://localhost:8086)", request.uri());
                    // CRITICAL: You must call next.handle(request) and return its result
                    // for the request to be forwarded.
                    return next.handle(request);
                })
                .build(); // The .build() call was misplaced inside the filter lambda.
    }
}