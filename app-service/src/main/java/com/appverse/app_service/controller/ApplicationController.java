package com.appverse.app_service.controller;

import com.appverse.app_service.dto.ApplicationRequest;
import com.appverse.app_service.dto.ApplicationResponse;
import com.appverse.app_service.dto.MessageResponse;
import com.appverse.app_service.dto.ScreenshotRequest;
import com.appverse.app_service.dto.UpdateApplicationRequest;
import com.appverse.app_service.model.Application;
import com.appverse.app_service.repository.ApplicationRepository;
import com.appverse.app_service.services.ApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;

    @PostMapping
    public ResponseEntity<MessageResponse> create(@RequestPart("data") @Valid ApplicationRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "screenshots", required = false) List<MultipartFile> screenshots,
            List<ScreenshotRequest> metadata) {
        return ResponseEntity.ok(applicationService.createApplication(request, thumbnail, screenshots, metadata));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> update(@PathVariable String id,
            @Valid @RequestBody UpdateApplicationRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "screenshots", required = false) List<MultipartFile> screenshots,
            @RequestPart(value = "metadata", required = false) List<ScreenshotRequest> metadata) {
        return ResponseEntity.ok(applicationService.updateApplication(id, request, thumbnail, screenshots, metadata));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        // return ResponseEntity.ok(applicationService.getAllApplications());
        logger.info("APP-SERVICE: /api/apps getAll() called. Attempting repository.findAll().");
        try {
            List<Application> applications = applicationRepository.findAll(); // Or use count()
            logger.info("APP-SERVICE: Successfully retrieved {} applications.", applications.size());
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            logger.error("APP-SERVICE: CRITICAL ERROR during repository.findAll() in getAll()", e); // 'e' will print
                                                                                                    // the stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error accessing database: " + e.getMessage());
        }
    }

    // In your AppController for app-service
    @GetMapping("/api/apps/test")
    public String testEndpoint() {
        return "App service test endpoint is working!";
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(@AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            return ResponseEntity.ok(jwt.getClaims());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No JWT token present");
        }
    }

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(Authentication authentication) {
        System.out.println("Authorities: " + authentication.getAuthorities());
        return ResponseEntity.ok("Auth OK");
    }

}
