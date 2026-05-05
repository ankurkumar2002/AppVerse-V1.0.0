package com.appverse.api_gateway.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Component
public class GatewayAuthorizationFilter {

    public ServerResponse authorize(
            ServerRequest request,
            RoutePolicy policy,
            HandlerFunction<ServerResponse> next) throws Exception {

        // Public Route
        if (policy.allowedRoles().isEmpty()) {
            return next.handle(request);
        }

        var principal = request.principal().orElse(null);

        if (!(principal instanceof JwtAuthenticationToken jwtAuth)) {
            return ServerResponse.status(401).build();
        }

        Jwt jwt = jwtAuth.getToken();

        Set<String> roles = extractRoles(jwt);

        boolean allowed = roles.stream()
                .anyMatch(policy.allowedRoles()::contains);

        if (!allowed) {
            return ServerResponse.status(403)
                    .body(Map.of(
                            "error", "FORBIDDEN",
                            "message", "You do not have necessary permission to access this resource"));
        }

        ServerRequest trusted = ServerRequest.from(request)
                .header("X-Keycloak-Id", jwt.getSubject())
                .header("X-Internal-Call", "true")
                .build();

        return next.handle(trusted);
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess =
                (Map<String, Object>) jwt.getClaims().get("realm_access");

        if (realmAccess == null) return Set.of();

        List<String> roles = (List<String>) realmAccess.get("roles");

        if (roles == null) return Set.of();

        return roles.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }
}