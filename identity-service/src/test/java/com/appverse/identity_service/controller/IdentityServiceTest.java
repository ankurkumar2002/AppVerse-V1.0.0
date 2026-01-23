package com.appverse.identity_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import com.appverse.identity_service.dto.IdentityUserResponse;
import com.appverse.identity_service.keycloakClient.KeycloakClient;
import com.appverse.identity_service.service.impl.IdentityServiceImpl;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock
    private KeycloakClient keycloakClient;

    @InjectMocks
    private IdentityServiceImpl identityService;

    @Test
    void shouldReturnUserById() {
        UserRepresentation user = new UserRepresentation();
        user.setId("123");
        user.setUsername("ankur");
        user.setEmail("ankur@test.com");
        user.setEmailVerified(true);
        user.setFirstName("Ankur");
        user.setLastName("Singh");

        when(keycloakClient.getUser("123")).thenReturn(user);

        IdentityUserResponse response = identityService.getUserById("123");

        assertEquals("123", response.id());
        assertEquals("ankur", response.username());
        assertEquals("ankur@test.com", response.email());
    }

    @Test
    void shouldAssignRole() {
        identityService.assignRole("123", List.of("DEVELOPER"));

        verify(keycloakClient).assignRole("123", "DEVELOPER");
    }

    @Test
    void shouldIgnoreNullRoles() {
        identityService.assignRole("123", null);

        verifyNoInteractions(keycloakClient);
    }

    @Test
    void shouldDisableUser() {
        identityService.disableUser("123");

        verify(keycloakClient).disableUser("123");
    }

    @Test
    void shouldReturnCurrentUserFromJwt() {
        Jwt jwt = Jwt.withTokenValue("token")
                .subject("123")
                .claim("preferred_username", "ankur")
                .claim("email", "ankur@test.com")
                .claim("email_verified", true)
                .claim("given_name", "Ankur")
                .claim("family_name", "Singh")
                .header("alg", "none")
                .build();

        Authentication auth = new Authentication() {
            @Override
            public Object getPrincipal() {
                return jwt;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean b) {
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }
        };

        IdentityUserResponse response = identityService.getCurrentUser(jwt);

        assertEquals("123", response.id());
        assertEquals("ankur", response.username());
    }

    @Test
    void shouldThrowExceptionForInvalidAuth() {
        Authentication auth = new Authentication() {
            @Override
            public Object getPrincipal() {
                return "not-a-jwt";
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean b) {
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }
        };

        assertThrows(IllegalStateException.class,
                () -> identityService.getCurrentUser(null));
    }

}
