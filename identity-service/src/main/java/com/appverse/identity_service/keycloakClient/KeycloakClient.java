package com.appverse.identity_service.keycloakClient;

import org.keycloak.representations.idm.UserRepresentation;

import com.appverse.identity_service.dto.UpdateIdentityRequest;
import com.appverse.identity_service.dto.UpdatePasswordRequest;

public interface KeycloakClient {

    UserRepresentation getUser(String userId);

    void assignRole(String userId, String role);

    void disableUser(String userId);
    
    UserRepresentation updateUser(String userId, UpdateIdentityRequest request);

    void updatePassword(String keycloakUserId, UpdatePasswordRequest request);

}