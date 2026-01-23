package com.appverse.identity_service.keycloakClient;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeycloakClientImpl implements KeycloakClient {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public KeycloakClientImpl(Keycloak keycloak){
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
    
}
