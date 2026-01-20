package com.appverse.identity_service.service.impl;

import com.appverse.identity_service.dto.IdentityUserResponse;
import com.appverse.identity_service.service.IdentityService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Service

public class IdentityServiceImpl implements IdentityService {

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm = "";

    private static final Logger log = Logger.getLogger(IdentityServiceImpl.class.getName());

    @Override
    public IdentityUserResponse getUserById(String keycloakUserId) {
        UserResource userResource = keycloak.realm(realm)
                .users()
                .get(keycloakUserId);

        UserRepresentation user = userResource.toRepresentation();

        return new IdentityUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEmailVerified(),
                user.getFirstName(),
                user.getLastName());
    }

    @Override
    public void assignRole(String keycloakUserId, List<String> roles) {
        UserResource userResource = keycloak.realm(realm)
                .users()
                .get(keycloakUserId);

        for (String role : roles) {
            if (role == null || role.isBlank()) {
                continue;
            }

            RoleRepresentation roleRep = keycloak.realm(realm)
                    .roles()
                    .get(role.toLowerCase())
                    .toRepresentation();

            userResource.roles()
                    .realmLevel()
                    .add(Collections.singletonList(roleRep));
        }
    }

    @Override
    public void disableUser(String keycloakUserId) {
        UserResource userResource = keycloak.realm(realm)
                .users()
                .get(keycloakUserId);

        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(false);

        userResource.update(user);
    }

    @Override
    public IdentityUserResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Invalid authentication context");
        }

        IdentityUserResponse response = new IdentityUserResponse(
                jwt.getSubject(),
                jwt.getClaim("preferred_username"),
                jwt.getClaim("email"),
                Boolean.TRUE.equals(jwt.getClaim("email_verified")),
                jwt.getClaim("given_name"),
                jwt.getClaim("family_name"));

        log.info(response.id());
        return response;
    }

}
