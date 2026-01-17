package com.appverse.user_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.appverse.user_service.dto.AssignRoleRequest;
import com.appverse.user_service.dto.IdentityUserResponse;

@FeignClient(name = "identity-service", url = "${services.identity-service.url}")
public interface IdentityClient {
    @GetMapping("/api/identity/users/{userId}")
    IdentityUserResponse getUserById(@PathVariable String userId);

    @PostMapping("/api/identity/users/{userId}/roles")
    void assignRoles(@PathVariable String userId, @RequestBody AssignRoleRequest request);

        @PostMapping("/api/identity/users/{userId}/disable")
    void disableUser(@PathVariable String userId);
}