package com.appverse.identity_service.service.impl;

import com.appverse.identity_service.dto.IdentityUserResponse;
import com.appverse.identity_service.dto.UpdateIdentityRequest;
import com.appverse.identity_service.dto.UpdatePasswordRequest;
import com.appverse.identity_service.keycloakClient.KeycloakClient;
import com.appverse.identity_service.service.IdentityService;

import lombok.extern.slf4j.Slf4j;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class IdentityServiceImpl implements IdentityService {

    @Autowired
    private KeycloakClient keycloak;

    @Value("${keycloak.realm}")
    private String realm = "";

    @Override
    public IdentityUserResponse getUserById(String keycloakUserId) {
        System.out.println(keycloakUserId);

        UserRepresentation user = keycloak.getUser(keycloakUserId);
        System.out.println(user.getEmail());

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
        System.out.println(roles);
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
        try {
            System.out.println(jwt);
            if (jwt == null) {
                throw new IllegalStateException("Invalid authentication context");
            }

            log.info("===== CURRENT JWT CLAIMS =====");
            log.info("SUB: " + jwt.getSubject());
            log.info("preferred_username: " + jwt.getClaim("preferred_username"));
            log.info("email: " + jwt.getClaim("email"));
            log.info("given_name: " + jwt.getClaim("given_name"));
            log.info("family_name: " + jwt.getClaim("family_name"));
            log.info("azp/client_id: " + jwt.getClaim("azp"));
            log.info("ALL CLAIMS: " + jwt.getClaims());

            return new IdentityUserResponse(
                    jwt.getSubject(),
                    jwt.getClaim("preferred_username"),
                    jwt.getClaim("email"),
                    Boolean.TRUE.equals(jwt.getClaim("email_verified")),
                    jwt.getClaim("given_name"),
                    jwt.getClaim("family_name"));

        } catch (Exception e) {
            log.error("Create developer failed", e);
            throw e;
        }
    }

    @Override
    public IdentityUserResponse updateUser(String keycloakUserId, UpdateIdentityRequest request) {
        UserRepresentation user = keycloak.updateUser(keycloakUserId, request);

        return new IdentityUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.isEmailVerified(),
                user.getFirstName(), user.getLastName());
    }

    @Override
    public void updatePassword(String keycloakUserId, UpdatePasswordRequest request) {
        keycloak.updatePassword(keycloakUserId, request);
    }

}
