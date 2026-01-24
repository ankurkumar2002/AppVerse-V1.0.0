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

import com.appverse.api_gateway.config.ServiceConfig;
import com.appverse.api_gateway.config.SimpleRateLimiter;

@Component
public class ApiHelperMethod {

    private static final Logger logger = LoggerFactory.getLogger(ApiHelperMethod.class);

    private final SimpleRateLimiter rateLimiter;

    public ApiHelperMethod(SimpleRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public RouterFunction<ServerResponse> apiRoute(ServiceConfig service) {

        return GatewayRouterFunctions.route(service.name())

                /* ================= RATE LIMIT ================= */
                .filter((request, next) -> {
                    String key = request.headers().firstHeader("Authorization");

                    if (!rateLimiter.allow(key)) {
                        logger.warn("[RATE_LIMIT] {}", request.uri());
                        return ServerResponse.status(429)
                                .header("X-Rate-Limited", "true")
                                .build();
                    }
                    return next.handle(request);
                })

                /* ================= HEADER SANITIZATION ================= */
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

                /* ================= TRACE ID ================= */
                .filter((request, next) -> {
                    String traceId = UUID.randomUUID().toString();

                    ServerRequest tracedRequest = ServerRequest.from(request)
                                    .header("X-Trace-Id", traceId)
                                    .build();
                    ServerResponse response = next.handle(tracedRequest);
                        
                            return ServerResponse.from(response)
                            .header("X-Trace-Id", traceId)
                            .build();
                })

                /* ================= CIRCUIT BREAKER ================= */
                .filter(
                        CircuitBreakerFilterFunctions.circuitBreaker(
                                service.cbName(),
                                URI.create("forward:/fallback")))

                /* ================= ROUTE ================= */
                .route(
                        RequestPredicates.path(service.basePath()),
                        HandlerFunctions.http(service.url()))

                /* ================= LOGGING ================= */
                .filter((request, next) -> {
                    logger.info("[API] {} -> {}", request.uri(), service.url());
                    return next.handle(request);
                })

                .build();
    }
}
