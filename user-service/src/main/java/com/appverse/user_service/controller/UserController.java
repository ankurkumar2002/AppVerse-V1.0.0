package com.appverse.user_service.controller;

import com.appverse.user_service.dto.KeycloakUpdateRequest;
import com.appverse.user_service.dto.MessageResponse;
import com.appverse.user_service.dto.UpdatePasswordRequest;
import com.appverse.user_service.dto.UpdateUserProfileRequest;
import com.appverse.user_service.dto.UserRequest;
import com.appverse.user_service.dto.UserResponse;
import com.appverse.user_service.repository.UserRepository;
import com.appverse.user_service.service.UserService;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> createMyProfile(
            @Valid @RequestBody UserRequest request) {

        MessageResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponse> getMyProfile() {
         System.out.println("CONTROLLER HIT");
        return ResponseEntity.ok(userService.getMyprofile());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> updateMyProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(request));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyAccount() {
        userService.deleteMyAccount();
    }

    @GetMapping("/internal/exists/{keycloakUserId}")
    @PreAuthorize("hasAuthority('SCOPE_internal')")
    public ResponseEntity<Map<String, Boolean>> existsInternal(@PathVariable String keycloakUserId) {
        System.out.println("Checkng if user exists");
        boolean exists = userRepository.existsByKeycloakUserId(keycloakUserId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/keycloak/{keycloakUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> userExists(@PathVariable String keycloakUserId) {
        boolean exists = userRepository.existsByKeycloakUserId(keycloakUserId);
        return ResponseEntity.ok(exists);
    }

    @PutMapping("/updatepassword")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> updatePassword(@RequestBody UpdatePasswordRequest request) {
        return ResponseEntity.ok(userService.updatePassword( request));
    }

    @PatchMapping("/updateprofile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> updateUser(@RequestBody KeycloakUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

}