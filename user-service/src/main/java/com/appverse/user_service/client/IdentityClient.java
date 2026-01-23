package com.appverse.user_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.appverse.user_service.dto.AssignRoleRequest;
import com.appverse.user_service.dto.IdentityUserResponse;

@FeignClient(name = "identity-service", url = "${services.identity-service.url}", configuration = FeignServiceAuthInterceptor.class)
public interface IdentityClient {
    @GetMapping("/api/identity/users/{keycloakUserId}")
    IdentityUserResponse getUserById(@PathVariable("keycloakUserId") String userId);

    @PostMapping("/api/identity/users/{keycloakUserId}/roles")
    void assignRoles(@PathVariable("keycloakUserId") String userId, @RequestBody AssignRoleRequest request);

    @PostMapping("/api/identity/users/{keycloakUserId}/disable")
    void disableUser(@PathVariable("keycloakUserId") String userId);

    
}