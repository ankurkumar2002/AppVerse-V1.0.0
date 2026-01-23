package com.appverse.api_gateway.routes.HelperMethods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import com.appverse.api_gateway.config.ServiceConfig;

@Component
public class ApiHelperMethod {

    private static final Logger logger = LoggerFactory.getLogger(ApiHelperMethod.class);

    public RouterFunction<ServerResponse> apiRoute(ServiceConfig service) {

        return GatewayRouterFunctions.route(service.name())
                .route(RequestPredicates.path(service.basePath()),
                        HandlerFunctions.http(service.url()))
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker(service.cbName()))
                .filter((request, next) -> {
                    logger.info("[API] {} -> {}", request.uri(), service.url());
                    return next.handle(request);
                })
                .build();
    }
}
