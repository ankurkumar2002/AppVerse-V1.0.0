package com.appverse.developer_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.appverse.developer_service.dto.AssignRoleRequest;
import com.appverse.developer_service.dto.IdentityUserResponse;
import com.appverse.developer_service.dto.KeycloakUserUpdateRequest;
import com.appverse.developer_service.dto.UpdatePasswordRequest;

@FeignClient(name = "identity-service", url = "${services.identity-service.url}", configuration = FeignServiceAuthInterceptor.class)
public interface IdentityClient {
    @GetMapping("/api/identity/users/{keycloakUserId}")
    IdentityUserResponse getUserById(@PathVariable("keycloakUserId") String userId);

    @PostMapping("/api/identity/users/{keycloakUserId}/roles")
    void assignRoles(@PathVariable("keycloakUserId") String userId, @RequestBody AssignRoleRequest request);

    @PostMapping("/api/identity/users/{keycloakUserId}/disable")
    void disableUser(@PathVariable("keycloakUserId") String userId);

    @PutMapping("/api/me/password/{keycloakUserId}")
    ResponseEntity<Void> updatePassword(@PathVariable("keycloakUserId") String keycloakUserId, UpdatePasswordRequest request);

    @PatchMapping("/api/{keycloakUserId}/update")
    IdentityUserResponse updateUser(@PathVariable("keycloakUserId") String keycloakUserId, KeycloakUserUpdateRequest request);

}
