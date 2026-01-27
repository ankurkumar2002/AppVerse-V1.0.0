package com.appverse.developer_service.controller;

import com.appverse.developer_service.dto.DeveloperEmailResponse;
import com.appverse.developer_service.dto.DeveloperRequest;
import com.appverse.developer_service.dto.DeveloperResponse;
import com.appverse.developer_service.dto.MessageResponse;
import com.appverse.developer_service.service.DeveloperService;
import com.appverse.developer_service.validation.OnCreate;
import com.appverse.developer_service.validation.OnUpdate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
@Slf4j
public class DeveloperController {

    private final DeveloperService developerService;

    @PostMapping
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> create(
            @Validated(OnCreate.class) @RequestBody DeveloperRequest request) {
        log.error("🔥🔥 CONTROLLER HIT 🔥🔥");
        MessageResponse response = developerService.createDeveloper(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> update(
            @PathVariable String id,
            @Validated(OnUpdate.class) @RequestBody DeveloperRequest request) throws AccessDeniedException {
        return ResponseEntity.ok(developerService.updateDeveloper(id, request));
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('developer')")
    public ResponseEntity<Void> delete(@PathVariable String id) throws AccessDeniedException {
        developerService.deleteDeveloper(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<DeveloperResponse> getDeveloperDetails() {
        return ResponseEntity.ok(developerService.getMyDeveloper());
    }

    @GetMapping("/exists/by-keycloak-id/{keycloakUserId}")
    public ResponseEntity<Boolean> existsByKeycloakId(@PathVariable String keycloakUserId) {
        return ResponseEntity.ok(
                developerService.existsByKeycloakUserId(keycloakUserId));
    }

    @GetMapping("/internal/{developerId}/email")
    public ResponseEntity<DeveloperEmailResponse> getDeveloperEmail(
            @PathVariable String developerId) {

        return ResponseEntity.ok(
                developerService.getDeveloperEmail(developerId));
    }

}
