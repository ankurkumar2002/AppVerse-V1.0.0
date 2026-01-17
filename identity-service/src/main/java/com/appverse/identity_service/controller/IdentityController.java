package com.appverse.identity_service.controller;

import com.appverse.identity_service.dto.AssignRoleRequest;
import com.appverse.identity_service.dto.IdentityUserResponse;
import com.appverse.identity_service.service.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity/users")
public class IdentityController {

    @Autowired
    private IdentityService identityService;

    @GetMapping("/{keycloakUserId}")
    public IdentityUserResponse getUserById(@PathVariable String keycloakUserId) {
        return identityService.getUserById(keycloakUserId);
    }

    @PostMapping("/{keycloakUserId}/roles")
    public void assignRole(
            @PathVariable String keycloakUserId,
            @RequestBody AssignRoleRequest request) {
        identityService.assignRole(keycloakUserId, request.roles());
    }
    @PostMapping("/{keycloakUserId}/disable")
public void disableUser(@PathVariable String keycloakUserId) {
    identityService.disableUser(keycloakUserId);
}


}
