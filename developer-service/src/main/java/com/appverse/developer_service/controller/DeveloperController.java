package com.appverse.developer_service.controller;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
@Slf4j
public class DeveloperController {

    // private static final Logger log =
    // LoggerFactory.getLogger(DeveloperController.class);
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
    @PreAuthorize("hasRole('developer')")
    public ResponseEntity<MessageResponse> update(
            @PathVariable String id,
            @Validated(OnUpdate.class) @RequestBody DeveloperRequest request) {
        return ResponseEntity.ok(developerService.updateDeveloper(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('developer')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        developerService.deleteDeveloper(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('developer')")
    public ResponseEntity<DeveloperResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(developerService.getDeveloperById(id));
    }

    @GetMapping
    // @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<List<DeveloperResponse>> getAll() {
        return ResponseEntity.ok(developerService.getAll());
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> exists(@RequestParam String id) {
        return ResponseEntity.ok(developerService.existsById(id));
    }

    @GetMapping("/exists/by-keycloak-id/{keycloakUserId}")
    public ResponseEntity<Boolean> checkExistsByKeycloakUserId(@PathVariable String keycloakUserId) {
        boolean exists = developerService.existsByKeycloakUserId(keycloakUserId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/is-profile-complete")
    public ResponseEntity<Map<String, Object>> checkProfile(
            @AuthenticationPrincipal Jwt jwt) {

        String keycloakUserId = jwt.getSubject(); // gets 'sub' from JWT

        boolean isComplete = developerService.isDeveloperProfileComplete(keycloakUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("profileComplete", isComplete);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
public ResponseEntity<DeveloperResponse> getCurrentDeveloper(@AuthenticationPrincipal Jwt jwt) {
    String keycloakUserId = jwt.getSubject();
    DeveloperResponse developer = developerService.getDeveloperByKeycloakUserId(keycloakUserId);
    return ResponseEntity.ok(developer);
}

@GetMapping("/is-developer/{id}")
public ResponseEntity<Boolean> isDeveloperById(@PathVariable String id) {
    boolean exists = developerService.existsById(id);
    return ResponseEntity.ok(exists);
}


    

}
