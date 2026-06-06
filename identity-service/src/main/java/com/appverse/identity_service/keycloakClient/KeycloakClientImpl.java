package com.appverse.identity_service.keycloakClient;

import java.util.List;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.appverse.identity_service.dto.UpdateIdentityRequest;
import com.appverse.identity_service.dto.UpdatePasswordRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KeycloakClientImpl implements KeycloakClient {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.serverUrl}")
    private String serverUrl;

    @Value("${keycloak.clientId}")
    private String clientId;

    @Value("${keycloak.clientSecret}")
    private String clientSecret;

    public KeycloakClientImpl(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    @Override
    public UserRepresentation getUser(String userId) {
        return keycloak.realm(realm)
                .users()
                .get(userId)
                .toRepresentation();
    }

    @Override
    public void assignRole(String userId, String role) {
        RoleRepresentation roleRep = keycloak.realm(realm)
                .roles()
                .get(role.toLowerCase())
                .toRepresentation();

        keycloak.realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(roleRep));
    }

    @Override
    public void disableUser(String userId) {
        UserResource userResource = keycloak.realm(realm)
                .users()
                .get(userId);

        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(false);
        userResource.update(user);
    }

    @Override
    public UserRepresentation updateUser(String userId, UpdateIdentityRequest request) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);

        UserRepresentation user = userResource.toRepresentation();

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());

            user.setEmailVerified(false);
        }

        userResource.update(user);
        log.info("User updated successfully "+ user.getEmail());

        return userResource.toRepresentation();
    }

    @Override
    public void updatePassword(String keycloakUserId, UpdatePasswordRequest request) {

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException(
                    "New password and confirm password do not match");
        }

        if (request.getCurrentPassword()
                .equals(request.getNewPassword())) {
            throw new IllegalArgumentException(
                    "New password must be different from current password");
        }

        UserResource userResource = keycloak.realm(realm).users().get(keycloakUserId);

        UserRepresentation user = userResource.toRepresentation();

        validateCredentials(user.getUsername(), request.getCurrentPassword());

        CredentialRepresentation credential = new CredentialRepresentation();

        credential.setType(CredentialRepresentation.PASSWORD);

        credential.setValue(request.getNewPassword());

        credential.setTemporary(false);
        try {
            userResource.resetPassword(credential);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Password does not satisfy policy requirements");
        }
    }

    private void validateCredentials(String username, String currentPassword) {
        Keycloak validationClient = null;
        try {
            validationClient = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(username)
                    .password(currentPassword)
                    .grantType(OAuth2Constants.PASSWORD)
                    .build();

            validationClient.tokenManager().getAccessToken();

        } catch (Exception e) {
            throw new IllegalArgumentException("Current password is incorrect");
        } finally {
            if (validationClient != null) {
                validationClient.close();
            }
        }
    }

}
