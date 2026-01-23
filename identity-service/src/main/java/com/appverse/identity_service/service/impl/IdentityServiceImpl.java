package com.appverse.identity_service.service.impl;

import com.appverse.identity_service.dto.IdentityUserResponse;
import com.appverse.identity_service.keycloakClient.KeycloakClient;
import com.appverse.identity_service.service.IdentityService;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class IdentityServiceImpl implements IdentityService {

    @Autowired
    private KeycloakClient keycloak;

    @Value("${keycloak.realm}")
    private String realm = "";

    private static final Logger log = Logger.getLogger(IdentityServiceImpl.class.getName());

    @Override
    public IdentityUserResponse getUserById(String keycloakUserId) {

        UserRepresentation user = keycloak.getUser(keycloakUserId);

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
        if (roles == null || roles.isEmpty()) {
            return;
        }

        roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .forEach(role -> keycloak.assignRole(keycloakUserId, role));
    }

    @Override
    public void disableUser(String keycloakUserId) {
        keycloak.disableUser(keycloakUserId);
    }

    @Override
    public IdentityUserResponse getCurrentUser(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalStateException("Invalid authentication context");
        }

        return new IdentityUserResponse(
                jwt.getSubject(),
                jwt.getClaim("preferred_username"),
                jwt.getClaim("email"),
                Boolean.TRUE.equals(jwt.getClaim("email_verified")),
                jwt.getClaim("given_name"),
                jwt.getClaim("family_name"));
    }

    

}
