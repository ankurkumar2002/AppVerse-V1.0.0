package com.appverse.identity_service.controller;

import com.appverse.identity_service.dto.AssignRoleRequest;
import com.appverse.identity_service.dto.IdentityUserResponse;
import com.appverse.identity_service.dto.UpdateIdentityRequest;
import com.appverse.identity_service.dto.UpdatePasswordRequest;
import com.appverse.identity_service.service.IdentityService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity/users")
@Slf4j
public class IdentityController {

    @Autowired
    private IdentityService identityService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public IdentityUserResponse me(
            @AuthenticationPrincipal Jwt jwt) {
        System.out.println(jwt);
        return identityService.getCurrentUser(jwt);
    }

    @GetMapping("/{keycloakUserId}")
    @PreAuthorize("hasAuthority('SCOPE_internal')")
    public IdentityUserResponse getUserById(@PathVariable String keycloakUserId) {
        return identityService.getUserById(keycloakUserId);
    }

    @PostMapping("/{keycloakUserId}/roles")
    @PreAuthorize("hasAuthority('SCOPE_internal')")
    public void assignRole(
            @PathVariable String keycloakUserId,
            @RequestBody AssignRoleRequest request) {
        identityService.assignRole(keycloakUserId, request.roles());
    }

    @PostMapping("/{keycloakUserId}/disable")
    @PreAuthorize("hasAuthority('SCOPE_internal')")
    public void disableUser(@PathVariable String keycloakUserId) {
        identityService.disableUser(keycloakUserId);
    }

    @PutMapping("/{keycloakUserId}/update")
    @PreAuthorize("hasAuthority('SCOPE_internal')")
    public ResponseEntity<IdentityUserResponse> updateUser(@PathVariable String keycloakUserId,
            @RequestBody UpdateIdentityRequest request) {
        log.info("Request reached here successfully!");
        log.info("REQUEST EMAIL = {}", request.getEmail());
        log.info("REQUEST FIRSTNAME = {}", request.getFirstName());
        log.info("REQUEST LASTNAME = {}", request.getLastName());
        return ResponseEntity.ok(identityService.updateUser(keycloakUserId, request));
    }

    @PutMapping("/me/password/{keycloakUserId}")
    @PreAuthorize("hasAuthority('SCOPE_internal')")
    public ResponseEntity<Void> updatePassword(@PathVariable String keycloakUserId,
            @RequestBody UpdatePasswordRequest request) {
        identityService.updatePassword(keycloakUserId, request);
        return ResponseEntity.noContent().build();
    }

}
