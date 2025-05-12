package com.appverse.user_service.controller;

import com.appverse.user_service.dto.UserRequest;
import com.appverse.user_service.dto.UserResponse;
// import com.appverse.user_service.dto.MessageResponse; // If you use it for some responses
import com.appverse.user_service.enums.Role;
import com.appverse.user_service.enums.UserStatus;
import com.appverse.user_service.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users") // Using v1 for versioning example
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    // --- User Creation ---
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_manage:users')") // Example: Only ADMINs or services with specific scope can create
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        log.info("Received request to create user for Keycloak ID: {}", userRequest.keycloakUserId());
        UserResponse createdUser = userService.createUser(userRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.id())
                .toUri();
        log.info("User created successfully with local ID: {}", createdUser.id());
        return ResponseEntity.created(location).body(createdUser);
    }

    // --- Get User Information ---
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(authentication, #userId) or hasAuthority('SCOPE_view:users')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        log.debug("Request to get user by local ID: {}", userId);
        UserResponse userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/keycloak/{keycloakUserId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwnerByKcUid(authentication, #keycloakUserId) or hasAuthority('SCOPE_view:users')")
    public ResponseEntity<UserResponse> getUserByKyloakUserId(@PathVariable String keycloakUserId) {
        log.debug("Request to get user by Keycloak ID: {}", keycloakUserId);
        UserResponse userResponse = userService.getUserByKyloakUserId(keycloakUserId); // Corrected method name
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_view:users')") // Typically admin or system lookup
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        log.debug("Request to get user by username: {}", username);
        UserResponse userResponse = userService.getUserByUsername(username);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_view:users')") // Typically admin or system lookup
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.debug("Request to get user by email: {}", email);
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping
    // @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_view:users:all')") // Example: More specific scope for listing all
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        // Consider adding Pageable here for production: (Pageable pageable)
        log.debug("Request to get all users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // --- Update User Information ---
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(authentication, #userId)")
    public ResponseEntity<UserResponse> updateUserProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UserRequest userRequest) {
        log.info("Request to update profile for user ID: {}", userId);
        UserResponse updatedUser = userService.updateUserProfile(userId, userRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // --- Admin Specific Updates ---
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam UserStatus newStatus) {
        // For isAdminAction, you might derive it from the caller's roles or pass explicitly
        log.info("Request to update status to {} for user ID: {}", newStatus, userId);
        UserResponse updatedUser = userService.updateUserStatus(userId, newStatus, true); // Assuming admin action
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable UUID userId,
            @RequestParam Role newRole) {
        log.info("Request to update role to {} for user ID: {}", newRole, userId);
        UserResponse updatedUser = userService.updateUserRole(userId, newRole);
        return ResponseEntity.ok(updatedUser);
    }

    // --- Delete User ---
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID userId) {
        log.info("Request to delete user ID: {}", userId);
        userService.deleteUser(userId);
    }

    // --- Utility ---
    @PostMapping("/{userId}/record-login")
    @PreAuthorize("isAuthenticated()") // Could be a system call or triggered after successful auth elsewhere
    public ResponseEntity<Void> recordUserLogin(@PathVariable UUID userId) {
        log.info("Request to record login for user ID: {}", userId);
        userService.recordUserLogin(userId);
        return ResponseEntity.ok().build();
    }

    // Example: Endpoint for the currently authenticated user to get their own profile
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            // This case should ideally be handled by Spring Security if endpoint is secured
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String keycloakUserId = jwt.getSubject(); // 'sub' claim is usually the Keycloak user ID
        log.debug("Request for current user's profile (Keycloak ID: {})", keycloakUserId);
        UserResponse userResponse = userService.getUserByKyloakUserId(keycloakUserId);
        return ResponseEntity.ok(userResponse);
    }
}