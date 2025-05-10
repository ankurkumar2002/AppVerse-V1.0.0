package com.appverse.developer_service.controller;

import com.appverse.developer_service.dto.DeveloperRequest;
import com.appverse.developer_service.dto.DeveloperResponse;
import com.appverse.developer_service.dto.MessageResponse;
import com.appverse.developer_service.service.DeveloperService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
public class DeveloperController {

    // private static final Logger log = LoggerFactory.getLogger(DeveloperController.class);
    private final DeveloperService developerService;

    @PostMapping
    @PreAuthorize("hasRole('developer')")
    public ResponseEntity<MessageResponse> create(
            @Valid @RequestBody DeveloperRequest request
    ) {
        

        MessageResponse response = developerService.createDeveloper(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('developer')")
    public ResponseEntity<MessageResponse> update(
            @PathVariable String id,
            @Valid @RequestBody DeveloperRequest request) {
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
    @PreAuthorize("hasRole('developer')")
    public ResponseEntity<List<DeveloperResponse>> getAll() {
        return ResponseEntity.ok(developerService.getAll());
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> exists(@RequestParam String id) {
        return ResponseEntity.ok(developerService.existsById(id));
    }

}
