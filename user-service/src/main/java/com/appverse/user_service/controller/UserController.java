package com.appverse.user_service.controller;

import com.appverse.user_service.dto.UpdateUserProfileRequest;
import com.appverse.user_service.dto.UserRequest;
import com.appverse.user_service.dto.UserResponse;
import com.appverse.user_service.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users") // Using v1 for versioning example
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("")
    public ResponseEntity<UserResponse> createMyProfile(
            @Valid @RequestBody UserRequest request) {

        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyprofile());
    }


    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {

        return ResponseEntity.ok(userService.updateUserProfile(request));
    }


    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyAccount() {
        userService.deleteMyAccount();
    }

    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> verifyExistence(@AuthenticationPrincipal Jwt jwt) {
        boolean exists = userService.checkUserExists(jwt.getSubject());
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    } 

    
}