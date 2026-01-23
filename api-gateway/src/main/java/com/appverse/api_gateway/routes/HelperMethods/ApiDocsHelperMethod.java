package com.appverse.api_gateway.routes.HelperMethods;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ApiDocsHelperMethod {

    private static final Logger logger =
            LoggerFactory.getLogger(ApiDocsHelperMethod.class);

    public RouterFunction<ServerResponse> apiDocsRoute(
            String routeId,
            String aggregatePath,
            String serviceUrl) {

        return GatewayRouterFunctions.route(routeId)
                .route(RequestPredicates.GET(aggregatePath),
                        HandlerFunctions.http(serviceUrl))
                .filter((request, next) -> {
                    ServerRequest modified = rewriteToApiDocs(request);
                    logger.info("[APIDOCS] {} -> {}", request.uri(), modified.uri());
                    return next.handle(modified);
                })
                .build();
    }

    private ServerRequest rewriteToApiDocs(ServerRequest request) {
        URI newUri = UriComponentsBuilder
                .fromUri(request.uri())
                .replacePath("/v3/api-docs")
                .build(true)
                .toUri();

        return ServerRequest.from(request)
                .uri(newUri)
                .build();
    }
}
