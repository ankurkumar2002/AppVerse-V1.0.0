package com.appverse.order_service.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
// import org.keycloak.admin.client.ResteasyClientBuilderProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminClientConfig {

    @Value("${appverse.keycloak.server-url}") // Changed prefix for clarity
    private String serverUrl;

    @Value("${appverse.keycloak.realm}")
    private String realm;

    @Value("${appverse.keycloak.client-id}")
    private String clientId;

    @Value("${appverse.keycloak.client-secret}")
    private String clientSecret;

    // Optional: If your Keycloak admin client user is in a different realm (e.g., 'master')
    // @Value("${appverse.keycloak.admin-realm:master}") // Defaults to 'master' if not set
    // private String adminRealm;

    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm) // The realm whose users you want to manage
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS) // Using client credentials grant
                .clientId(clientId) // The client ID in Keycloak used for admin operations
                .clientSecret(clientSecret) 
                .build();
    }
    
}