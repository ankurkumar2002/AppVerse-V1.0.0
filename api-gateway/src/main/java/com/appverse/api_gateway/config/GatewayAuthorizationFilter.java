package com.appverse.api_gateway.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import com.appverse.api_gateway.client.DeveloperClient;
import com.appverse.api_gateway.client.UserClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GatewayAuthorizationFilter {

    private final UserClient userClient;
    private final DeveloperClient developerClient;

    public ServerResponse authorize(
            ServerRequest request,
            RoutePolicy policy,
            HandlerFunction<ServerResponse> next) throws Exception {

        /* ========= PUBLIC ROUTES ========= */
        if (policy.allowedRoles().isEmpty()) {
            return next.handle(request);
        }

        /* ========= JWT FROM PRINCIPAL (MVC SAFE) ========= */
        var principal = request.principal().orElse(null);

        if (!(principal instanceof JwtAuthenticationToken jwtAuth)) {
            return ServerResponse.status(401).build();
        }

        Jwt jwt = jwtAuth.getToken();

        String keycloakUserId = jwt.getSubject();
        Set<String> roles = extractRoles(jwt);

        /* ========= ROLE CHECK ========= */
        boolean allowed = roles.stream().anyMatch(policy.allowedRoles()::contains);

        if (!allowed) {
            return ServerResponse.status(403)
                    .body(Map.of(
                            "error", "FORBIDDEN",
                            "message", "You do not have necessary permission to access this resource"));
        }

        if (!allowed) {
            return ServerResponse.status(403).build();
        }

        /* ========= PROFILE CHECK ========= */
        if (policy.requireProfile()) {

            boolean exists;

            if (policy.allowedRoles().contains("DEVELOPER")) {

                Boolean devExists = developerClient
                        .isDeveloperByKeycloakId(keycloakUserId)
                        .getBody();

                exists = Boolean.TRUE.equals(devExists);

            } else if (policy.allowedRoles().contains("USER")) {

                Map<String, Boolean> result = userClient.checkUserExists(keycloakUserId);
                exists = Boolean.TRUE.equals(result.get("exists"));

            } else {
                exists = true; // fallback safety
            }

            if (!exists) {
                return ServerResponse.status(403)
                        .body("Profile not completed");
            }
        }

        /* ========= TRUSTED HEADERS ========= */
        ServerRequest trusted = ServerRequest.from(request)
                .header("X-Keycloak-Id", keycloakUserId)
                .header("X-Internal-Call", "true")
                .build();

        return next.handle(trusted);
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

        if (realmAccess == null)
            return Set.of();

        List<String> roles = (List<String>) realmAccess.get("roles");

        if (roles == null)
            return Set.of();

        return roles.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }
}
