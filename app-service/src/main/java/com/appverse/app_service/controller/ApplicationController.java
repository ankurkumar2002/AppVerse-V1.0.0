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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
public class ApplicationController {

    @Value("${app.upload-dir}")
private String uploadDir;

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;
    private final ObjectMapper objectMapper; 

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) 
    public ResponseEntity<MessageResponse> create(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "screenshots", required = false) List<MultipartFile> screenshots,
            @RequestPart(value = "metadata", required = false) String metadataJson) {

        ApplicationRequest request;
        List<ScreenshotRequest> metadata = Collections.emptyList(); 

        try {
            request = objectMapper.readValue(requestJson, ApplicationRequest.class);


            if (metadataJson != null && !metadataJson.isBlank()) {
                 metadata = objectMapper.readValue(metadataJson, new TypeReference<List<ScreenshotRequest>>() {});
            }



        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON request part: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON format in 'request' or 'metadata' part", e);
        }


        System.out.println("Request object created: " + (request != null));
        System.out.println("Thumbnail type: " + (thumbnail != null ? thumbnail.getContentType() : "null"));
        System.out.println("Screenshots count: " + (screenshots != null ? screenshots.size() : 0));
        System.out.println("Metadata list created: " + (metadata != null));
        System.out.println("Metadata list size: " + (metadata != null ? metadata.size() : 0));


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
        logger.info("APP-SERVICE: /api/apps getAll() called. Attempting repository.findAll().");
        try {
            List<Application> applications = applicationRepository.findAll(); 
            logger.info("APP-SERVICE: Successfully retrieved {} applications.", applications.size());
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            logger.error("APP-SERVICE: CRITICAL ERROR during repository.findAll() in getAll()", e); 
                                                                                                    
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error accessing database: " + e.getMessage());
        }
    }

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

/// Note: I'm moving this endpoint out from under the "/api/apps" prefix
// because serving images is a general utility, not specific to an "app" resource.
// The new URL will be, for example, /images/thumbnails/my-image.jpg
@GetMapping("/images/{type}/{filename:.+}")
public ResponseEntity<Resource> getImage(@PathVariable String type, @PathVariable String filename) {
    // --- Security Check ---
    // Whitelist the allowed directories to prevent Path Traversal attacks.
    // A user should not be able to request a file with type = "../../some/other/folder".
    if (!"thumbnails".equals(type) && !"screenshots".equals(type)) {
        logger.warn("Invalid image type requested: {}", type);
        return ResponseEntity.badRequest().build();
    }

    // Build the correct path using the 'type' variable
    Path file = Paths.get(uploadDir).resolve(type).resolve(filename).normalize();

    logger.info("Attempting to serve image from path: {}", file.toAbsolutePath());

    Resource resource;
    try {
        resource = new UrlResource(file.toUri());
    } catch (IOException e) {
        logger.error("Could not create resource for file: {}", file.toAbsolutePath(), e);
        // This can happen if the filename contains invalid characters for a URI
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    if (resource.exists() && resource.isReadable()) {
        try {
            MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
        } catch(Exception e) {
             logger.error("Error determining media type for: {}", file.toAbsolutePath(), e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    } else {
        logger.error("Image not found or not readable at: {}", file.toAbsolutePath());
        return ResponseEntity.notFound().build();
    }
}






}
