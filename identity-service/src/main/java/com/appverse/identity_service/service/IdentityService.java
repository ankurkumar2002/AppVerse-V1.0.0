package com.appverse.identity_service.service;

import java.util.List;

import com.appverse.identity_service.dto.IdentityUserResponse;

public interface IdentityService {

    IdentityUserResponse getUserById(String keycloakUserId);


    void assignRole(String keycloakUserId, List<String> roles);


    void disableUser(String keycloakUserId);
}
