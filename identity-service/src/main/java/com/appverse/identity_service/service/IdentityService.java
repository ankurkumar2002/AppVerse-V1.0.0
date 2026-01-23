package com.appverse.identity_service.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import com.appverse.identity_service.dto.IdentityUserResponse;

public interface IdentityService {

    IdentityUserResponse getUserById(String keycloakUserId);


    void assignRole(String keycloakUserId, List<String> roles);


    void disableUser(String keycloakUserId);


    IdentityUserResponse getCurrentUser(Jwt jwt);
}
