package com.appverse.developer_service.controller;

import com.appverse.developer_service.dto.DeveloperEmailResponse;
import com.appverse.developer_service.dto.DeveloperRequest;
import com.appverse.developer_service.dto.DeveloperResponse;
import com.appverse.developer_service.dto.MessageResponse;
import com.appverse.developer_service.service.DeveloperService;
import com.appverse.developer_service.validation.OnCreate;
import com.appverse.developer_service.validation.OnUpdate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
@Slf4j
public class DeveloperController {

    private final DeveloperService developerService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> create(
            @Validated(OnCreate.class) @RequestBody DeveloperRequest request) {
        log.error("🔥🔥 CONTROLLER HIT 🔥🔥");
        MessageResponse response = developerService.createDeveloper(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<MessageResponse> update(
            @PathVariable String id,
            @Validated(OnUpdate.class) @RequestBody DeveloperRequest request) throws AccessDeniedException {
        return ResponseEntity.ok(developerService.updateDeveloper(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<Void> delete(@PathVariable String id) throws AccessDeniedException {
        developerService.deleteDeveloper(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<DeveloperResponse> getDeveloperDetails() {
        return ResponseEntity.ok(developerService.getMyDeveloper());
    }

    @GetMapping("/exists/by-keycloak-id/{keycloakUserId}")
    @PreAuthorize("hasAuthority('SCOPE_internal')")
    public ResponseEntity<Boolean> existsByKeycloakId(@PathVariable String keycloakUserId) {
        return ResponseEntity.ok(
                developerService.existsByKeycloakUserId(keycloakUserId));
    }

    @GetMapping("/internal/{developerId}/email")
    @PreAuthorize("hasAuthority('SCOPE_internal')")
    public ResponseEntity<DeveloperEmailResponse> getDeveloperEmail(
            @PathVariable String developerId) {

        return ResponseEntity.ok(
                developerService.getDeveloperEmail(developerId));
    }

}
