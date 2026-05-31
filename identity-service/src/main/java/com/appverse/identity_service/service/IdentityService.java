package com.appverse.identity_service.service;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;

import com.appverse.identity_service.dto.IdentityUserResponse;
import com.appverse.identity_service.dto.UpdateIdentityRequest;
import com.appverse.identity_service.dto.UpdatePasswordRequest;

public interface IdentityService {

    IdentityUserResponse getUserById(String keycloakUserId);


    void assignRole(String keycloakUserId, List<String> roles);


    void disableUser(String keycloakUserId);


    IdentityUserResponse getCurrentUser(Jwt jwt);

    IdentityUserResponse updateUser(String keycloakUserId, UpdateIdentityRequest request);

    void updatePassword(String keycloakUserId, UpdatePasswordRequest request);
}
