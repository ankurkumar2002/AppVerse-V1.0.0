package com.appverse.api_gateway.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.oauth2.jwt.Jwt;
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
            HandlerFunction<ServerResponse> next
    ) throws Exception {

        /* ========= PUBLIC ROUTES ========= */
        if (policy.allowedRoles().isEmpty()) {
            return next.handle(request);
        }

        /* ========= JWT REQUIRED ========= */
        Jwt jwt = request.attribute("jwt")
                .map(Jwt.class::cast)
                .orElse(null);

        if (jwt == null) {
            return ServerResponse.status(401).build();
        }

        String keycloakUserId = jwt.getSubject();
        Set<String> roles = extractRoles(jwt);

        /* ========= ROLE CHECK ========= */
        boolean allowed =
                roles.stream().anyMatch(policy.allowedRoles()::contains);

        if (!allowed) {
            return ServerResponse.status(403).build();
        }

        /* ========= PROFILE CHECK ========= */
        if (policy.requireProfile()) {

            boolean exists;

            if (roles.contains("DEVELOPER")) {
                exists = developerClient.isDeveloperByKeycloakId(keycloakUserId);
            } else {
                Map<String, Boolean> result = userClient.checkUserExists();
                exists = Boolean.TRUE.equals(result.get("exists"));
            }

            if (!exists) {
                return ServerResponse.status(403)
                        .body("Profile not completed");
            }
        }

        /* ========= TRUSTED HEADERS ========= */
        ServerRequest trusted = ServerRequest.from(request)
                .header("X-Keycloak-Id", keycloakUserId)
                .header("X-Role", roles.iterator().next())
                .header("X-Internal-Call", "true")
                .build();

        return next.handle(trusted);
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess =
                (Map<String, Object>) jwt.getClaims().get("realm_access");

        if (realmAccess == null) {
            return Set.of();
        }

        List<String> roles =
                (List<String>) realmAccess.get("roles");

        return roles == null ? Set.of() : Set.copyOf(roles);
    }
}
