package com.appverse.api_gateway.routes.HelperMethods;

import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import com.appverse.api_gateway.config.GatewayAuthorizationFilter;
import com.appverse.api_gateway.config.RoutePolicy;
import com.appverse.api_gateway.config.ServiceConfig;
import com.appverse.api_gateway.config.SimpleRateLimiter;

@Component
public class ApiHelperMethod {

    private static final Logger logger =
            LoggerFactory.getLogger(ApiHelperMethod.class);

    private final SimpleRateLimiter rateLimiter;
    private final GatewayAuthorizationFilter authorizationFilter;

    public ApiHelperMethod(
            SimpleRateLimiter rateLimiter,
            GatewayAuthorizationFilter authorizationFilter) {

        this.rateLimiter = rateLimiter;
        this.authorizationFilter = authorizationFilter;
    }

    public RouterFunction<ServerResponse> apiRoute(
            ServiceConfig service,
            RoutePolicy policy
    ) throws Exception {

        return GatewayRouterFunctions.route(service.name())

                /* ========= RATE LIMIT ========= */
                .filter((request, next) -> {
                    String key = request.headers().firstHeader("Authorization");
                    if (!rateLimiter.allow(key)) {
                        return ServerResponse.status(429).build();
                    }
                    return next.handle(request);
                })

                /* ========= HEADER SANITIZATION ========= */
                .filter((request, next) -> {
                    ServerRequest sanitized = ServerRequest.from(request)
                            .headers(h -> {
                                h.remove("X-User-Id");
                                h.remove("X-Internal-Call");
                                h.remove("X-Role");
                            })
                            .build();
                    return next.handle(sanitized);
                })

                /* ========= AUTHORIZATION (ROLE + PROFILE) ========= */
                .filter((request, next) ->
                        authorizationFilter.authorize(request, policy, next)
                )

                /* ========= TRACE ========= */
                .filter((request, next) -> {
                    ServerRequest traced = ServerRequest.from(request)
                            .header("X-Trace-Id", UUID.randomUUID().toString())
                            .build();
                    return next.handle(traced);
                })

                /* ========= CIRCUIT BREAKER ========= */
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        service.cbName(),
                        URI.create("forward:/fallback")
                ))

                /* ========= ROUTE ========= */
                .route(RequestPredicates.path(service.basePath()),
                        HandlerFunctions.http(service.url()))

                /* ========= LOG ========= */
                .filter((request, next) -> {
                    logger.info("[API] {} -> {}", request.uri(), service.url());
                    return next.handle(request);
                })

                .build();
    }
}
