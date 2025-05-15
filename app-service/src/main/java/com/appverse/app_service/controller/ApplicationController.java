package com.appverse.app_service.controller;

import com.appverse.app_service.dto.ApplicationRequest;
import com.appverse.app_service.dto.ApplicationResponse;
import com.appverse.app_service.dto.MessageResponse;
import com.appverse.app_service.dto.ScreenshotRequest;
import com.appverse.app_service.dto.UpdateApplicationRequest;
import com.appverse.app_service.model.Application;
import com.appverse.app_service.repository.ApplicationRepository;
import com.appverse.app_service.services.ApplicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;
    private final ObjectMapper objectMapper; 

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Explicitly state consumes
    public ResponseEntity<MessageResponse> create(
            @RequestPart("request") String requestJson, // Receive as String
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "screenshots", required = false) List<MultipartFile> screenshots,
            // Add @RequestPart, receive as String, make optional if appropriate
            @RequestPart(value = "metadata", required = false) String metadataJson) {

        ApplicationRequest request;
        List<ScreenshotRequest> metadata = Collections.emptyList(); // Default to empty list

        try {
            // Manually deserialize the request JSON string
            request = objectMapper.readValue(requestJson, ApplicationRequest.class);

            // Manually deserialize the metadata JSON string if present
            if (metadataJson != null && !metadataJson.isBlank()) {
                // Assuming metadata is expected as a JSON array of ScreenshotRequest objects
                 metadata = objectMapper.readValue(metadataJson, new TypeReference<List<ScreenshotRequest>>() {});
            }

            // Optional: Perform validation manually if needed, as @Valid won't work on the String
            // ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            // Validator validator = factory.getValidator();
            // Set<ConstraintViolation<ApplicationRequest>> violations = validator.validate(request);
            // if (!violations.isEmpty()) {
            //     // Handle validation errors appropriately
            //     throw new ConstraintViolationException(violations);
            // }


        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON request part: {}", e.getMessage());
            // Return a 400 Bad Request
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON format in 'request' or 'metadata' part", e);
        }

        // Log received data types after parsing
        System.out.println("Request object created: " + (request != null));
        System.out.println("Thumbnail type: " + (thumbnail != null ? thumbnail.getContentType() : "null"));
        System.out.println("Screenshots count: " + (screenshots != null ? screenshots.size() : 0));
        System.out.println("Metadata list created: " + (metadata != null));
        System.out.println("Metadata list size: " + (metadata != null ? metadata.size() : 0));


        // Call the service with the parsed objects
        return ResponseEntity.ok(applicationService.createApplication(request, thumbnail, screenshots, metadata));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> update(@PathVariable String id,
            @Valid @RequestPart UpdateApplicationRequest request,
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
