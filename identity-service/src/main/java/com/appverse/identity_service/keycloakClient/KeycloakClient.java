package com.appverse.identity_service.keycloakClient;

import org.keycloak.representations.idm.UserRepresentation;

public interface KeycloakClient {

    UserRepresentation getUser(String userId);

    void assignRole(String userId, String role);

    void disableUser(String userId);

}