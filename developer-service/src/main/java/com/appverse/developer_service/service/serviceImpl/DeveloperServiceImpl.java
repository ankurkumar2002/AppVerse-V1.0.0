// === In developer-service Project ===
package com.appverse.developer_service.service.serviceImpl;

import com.appverse.developer_service.dto.DeveloperRequest;
import com.appverse.developer_service.dto.DeveloperResponse;
import com.appverse.developer_service.dto.MessageResponse;
import com.appverse.developer_service.event.payload.*;
import com.appverse.developer_service.exception.*;
import com.appverse.developer_service.mapper.DeveloperMapper;
import com.appverse.developer_service.model.Developer;
import com.appverse.developer_service.repository.DeveloperRepository;
import com.appverse.developer_service.service.DeveloperService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult; // <<< IMPORT FOR COMPLETABLEFUTURE
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture; // <<< IMPORT FOR COMPLETABLEFUTURE

@Service
@RequiredArgsConstructor
@Slf4j
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;
    private final DeveloperMapper developerMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String DEVELOPER_EVENTS_TOPIC = "developer-events";
    private final RestTemplate restTemplate;

   @Override
@Transactional
public MessageResponse createDeveloper(DeveloperRequest request) {
    log.info("Attempting to create developer profile for email: {}", request.email());

    try {
        log.debug("[Step 1] Fetching Keycloak User ID from Security Context");
        String keycloakUserId = getKeycloakUserIdFromSecurityContext();
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            log.error("Keycloak User ID is missing from security context. Cannot create developer profile.");
            throw new IllegalArgumentException("Keycloak User ID must be provided for developer creation.");
        }
        log.debug("[Step 1 Completed] Keycloak User ID: {}", keycloakUserId);

        log.debug("[Step 2] Checking if developer profile already exists for Keycloak ID");
        if (developerRepository.existsByKeycloakUserId(keycloakUserId)) {
            throw new DuplicateResourceException(
                "Developer profile already exists for Keycloak user ID: " + keycloakUserId);
        }
        log.debug("[Step 2.1] Checking if email already exists: {}", request.email());
        if (developerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException(
                "Developer with email '" + request.email() + "' already exists.");
        }

        log.debug("[Step 3] Mapping DeveloperRequest to entity");
        Developer developer = developerMapper.toEntity(request);
        developer.setKeycloakUserId(keycloakUserId);

        log.debug("[Step 4] Fetching Keycloak admin token");
        String adminToken = getKeycloakAdminToken();
        log.debug("[Step 4 Completed] Admin token retrieved successfully");

        log.debug("[Step 5] Fetching Keycloak user info for ID: {}", keycloakUserId);
        Map<String, Object> keycloakUserInfo = getKeycloakUserInfo(keycloakUserId, adminToken);
        log.debug("[Step 5 Completed] Keycloak user info: {}", keycloakUserInfo);

        String name = (String) keycloakUserInfo.get("firstName") + " " + (String) keycloakUserInfo.get("lastName");
        String email = (String) keycloakUserInfo.get("email");

        developer.setEmail(email);
        developer.setName(name);

        log.debug("[Step 6] Saving developer entity to database");
        Developer savedDeveloper = developerRepository.save(developer);
        log.debug("[Step 6 Completed] Developer saved with ID: {}", savedDeveloper.getId());

        if (!isDeveloperProfileComplete(keycloakUserId)) {
            log.warn("Profile saved but considered incomplete.");
        }

        log.debug("[Step 7] Assigning 'developer' role to Keycloak user: {}", savedDeveloper.getKeycloakUserId());
        try {
            assignRoleToUser(savedDeveloper.getKeycloakUserId(), "developer", adminToken);
            log.info("Assigned 'developer' role to Keycloak user ID {}", savedDeveloper.getKeycloakUserId());
        } catch (Exception e) {
            log.error("Failed to assign role to Keycloak user: {}", e.getMessage(), e);
        }

        log.debug("[Step 8] Sending DeveloperProfileCreatedEvent to Kafka");
        DeveloperProfileCreatedPayload payload = new DeveloperProfileCreatedPayload(
                savedDeveloper.getId(),
                savedDeveloper.getKeycloakUserId(),
                savedDeveloper.getName(),
                savedDeveloper.getEmail(),
                savedDeveloper.getDeveloperType(),
                savedDeveloper.getCompanyName(),
                savedDeveloper.getCreatedAt());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                DEVELOPER_EVENTS_TOPIC, savedDeveloper.getId(), payload);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent DeveloperProfileCreatedEvent to topic {} for key {}: offset {}, partition {}",
                        DEVELOPER_EVENTS_TOPIC, savedDeveloper.getId(),
                        result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
            } else {
                log.error("Failed to send DeveloperProfileCreatedEvent to topic {} for key {}: {}",
                        DEVELOPER_EVENTS_TOPIC, savedDeveloper.getId(), ex.getMessage(), ex);
            }
        });

        log.debug("Returning success response to client");
        return new MessageResponse("Developer created successfully.", savedDeveloper.getId());

    } catch (DataAccessException e) {
        log.error("[Database Error] Failed to create developer: {}", e.getMessage(), e);
        throw new DatabaseOperationException("Failed to create developer due to a database issue." + e);
    } catch (Exception e) {
        log.error("[Unexpected Error] {}", e.getMessage(), e);
        throw e;
    }
}


    @Override
    @Transactional
    public MessageResponse updateDeveloper(String id, DeveloperRequest request) {
        log.info("Attempting to update developer profile with ID: {}", id);
        Developer existingDeveloper = developerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with ID: " + id));

        if (request.email() != null && !request.email().isBlank() &&
                !existingDeveloper.getEmail().equalsIgnoreCase(request.email()) &&
                developerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException(
                    "Cannot update: Another developer with email '" + request.email() + "' already exists.");
        }

        developerMapper.updateFromDto(request, existingDeveloper);

        try {
            Developer updatedDeveloper = developerRepository.save(existingDeveloper);
            log.info("Developer profile updated successfully with ID: {}", id);
            try {
                updateKeycloakUser(existingDeveloper.getKeycloakUserId(), request); // <-- Call Keycloak update here
                log.info("Keycloak user profile updated for userId: {}", existingDeveloper.getKeycloakUserId());
            } catch (Exception ex) {
                log.warn("Keycloak update failed: {}", ex.getMessage(), ex);
                // Optional: decide if this should fail the entire update or not
            }

            DeveloperProfileUpdatedPayload payload = new DeveloperProfileUpdatedPayload(
                    updatedDeveloper.getId(),
                    updatedDeveloper.getKeycloakUserId(),
                    updatedDeveloper.getName(),
                    updatedDeveloper.getEmail(),
                    updatedDeveloper.getDeveloperType(),
                    updatedDeveloper.getWebsite(),
                    updatedDeveloper.getCompanyName(),
                    updatedDeveloper.getBio(),
                    updatedDeveloper.getLogoUrl(),
                    updatedDeveloper.getLocation(),
                    updatedDeveloper.getUpdatedAt());

            // --- ENHANCED KAFKA SEND WITH LOGGING CALLBACK ---
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(DEVELOPER_EVENTS_TOPIC,
                    updatedDeveloper.getId(), payload);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info(
                            "Successfully sent DeveloperProfileUpdatedEvent to topic {} for key {}: offset {}, partition {}",
                            DEVELOPER_EVENTS_TOPIC, updatedDeveloper.getId(),
                            result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to send DeveloperProfileUpdatedEvent to topic {} for key {}: {}",
                            DEVELOPER_EVENTS_TOPIC, updatedDeveloper.getId(), ex.getMessage(), ex);
                }
            });
            log.debug("Asynchronously published DeveloperProfileUpdatedEvent for Developer ID: {}.",
                    updatedDeveloper.getId());

            return new MessageResponse("Developer updated successfully.", updatedDeveloper.getId());
        } catch (DataAccessException e) {
            log.error("Database error updating developer with ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to update developer due to a database issue." + e);
        }
    }

    @Override
    @Transactional
    public void deleteDeveloper(String id) {
        log.info("Attempting to delete developer profile with ID: {}", id);
        Developer developerToDelete = developerRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Developer not found with ID: " + id + ", cannot delete."));

        try {
            developerRepository.deleteById(id);
            log.info("Developer profile deleted successfully with ID: {}", id);

            DeveloperProfileDeletedPayload payload = new DeveloperProfileDeletedPayload(
                    developerToDelete.getId(),
                    developerToDelete.getKeycloakUserId(),
                    developerToDelete.getName(),
                    developerToDelete.getEmail(),
                    Instant.now());

            // --- ENHANCED KAFKA SEND WITH LOGGING CALLBACK ---
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(DEVELOPER_EVENTS_TOPIC,
                    developerToDelete.getId(), payload);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info(
                            "Successfully sent DeveloperProfileDeletedEvent to topic {} for key {}: offset {}, partition {}",
                            DEVELOPER_EVENTS_TOPIC, developerToDelete.getId(),
                            result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to send DeveloperProfileDeletedEvent to topic {} for key {}: {}",
                            DEVELOPER_EVENTS_TOPIC, developerToDelete.getId(), ex.getMessage(), ex);
                }
            });
            log.debug("Asynchronously published DeveloperProfileDeletedEvent for Developer ID: {}.",
                    developerToDelete.getId());

        } catch (DataAccessException e) {
            log.error("Database error deleting developer with ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to delete developer due to a database issue." + e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeveloperResponse getDeveloperById(String id) {
        log.debug("Fetching developer profile by ID: {}", id);
        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with ID: " + id));
        return developerMapper.toResponse(developer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeveloperResponse> getAll() {
        log.debug("Fetching all developer profiles.");
        try {
            List<Developer> developers = developerRepository.findAll();
            return developerMapper.toResponseList(developers);
        } catch (DataAccessException e) {
            log.error("Database error retrieving all developers: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Failed to retrieve developers due to a database issue." + e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String id) {
        log.debug("Checking existence for developer ID: {}", id);
        return developerRepository.existsById(id);
    }




    @Override
    @Transactional(readOnly = true)
    public boolean existsByKeycloakUserId(String keycloakUserId) {
        log.debug("Checking existence for developer with Keycloak User ID: {}", keycloakUserId);
        return developerRepository.existsByKeycloakUserId(keycloakUserId);
    }

    private String getKeycloakUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return jwt.getSubject();
        }
        log.warn("Could not retrieve Keycloak User ID from SecurityContext. Authentication was: {}", authentication);
        return null;
    }

    private String getKeycloakAdminToken() {
        String tokenUrl = "http://localhost:8181/realms/master/protocol/openid-connect/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", "admin-cli");
        formData.add("username", "admin"); // your Keycloak admin
        formData.add("password", "admin"); // your Keycloak admin password
        formData.add("grant_type", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return response.getBody().get("access_token").toString();
    }

    private void assignRoleToUser(String userId, String roleName, String token) {
        // Fetch available client roles from the realm (here assuming 'realm roles')
        String realmName = "appverse";
        String roleUrl = "http://localhost:8181/admin/realms/" + realmName + "/roles/" + roleName;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> roleResponse = restTemplate.exchange(
                roleUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        Map role = roleResponse.getBody();

        String assignUrl = "http://localhost:8181/admin/realms/" + realmName + "/users/" + userId
                + "/role-mappings/realm";

        List<Map<String, Object>> rolesToAssign = new ArrayList<>();
        rolesToAssign.add(Map.of(
                "id", role.get("id"),
                "name", role.get("name")));

        HttpEntity<List<Map<String, Object>>> assignRequest = new HttpEntity<>(rolesToAssign, headers);
        restTemplate.postForEntity(assignUrl, assignRequest, Void.class);
    }

    private Map<String, Object> getKeycloakUserInfo(String userId, String token) {
        String url = "http://localhost:8181/admin/realms/appverse/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, request, Map.class);

        return response.getBody();
    }

    public boolean isDeveloperProfileComplete(String keycloakUserId) {
        return developerRepository.findByKeycloakUserId(keycloakUserId)
                .map(developer -> developer.getName() != null &&
                        developer.getEmail() != null &&
                        developer.getDeveloperType() != null &&
                        developer.getRole() != null)
                .orElse(false); // false if no profile exists
    }

    @Override
    @Transactional(readOnly = true)
    public DeveloperResponse getDeveloperByKeycloakUserId(String keycloakUserId) {
        log.info("Fetching developer for Keycloak ID: {}", keycloakUserId);

        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            log.error("Keycloak User ID is null or empty");
            throw new IllegalArgumentException("Keycloak User ID must not be null or empty");
        }

        try {
            Developer developer = developerRepository.findByKeycloakUserId(keycloakUserId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Developer not found with Keycloak User ID: " + keycloakUserId));
            return developerMapper.toResponse(developer);
        } catch (DataAccessException e) {
            log.error("Database error fetching developer for Keycloak ID {}: {}", keycloakUserId, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to fetch developer due to a database issue." + e);
        }
    }

    private void updateKeycloakUser(String userId, DeveloperRequest request) {
    String realmName = "appverse";
    String url = "http://localhost:8181/admin/realms/" + realmName + "/users/" + userId;

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(getKeycloakAdminToken());
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> updateData = Map.of(
        "firstName", request.name() != null ? request.name().split(" ")[0] : "Developer",
        "lastName", request.name() != null && request.name().split(" ").length > 1 ? request.name().split(" ")[1] : "",
        "email", request.email()
    );

    HttpEntity<Map<String, Object>> updateRequest = new HttpEntity<>(updateData, headers);

    try {
        restTemplate.exchange(url, HttpMethod.PUT, updateRequest, Void.class);
        log.info("Keycloak user updated successfully for user ID: {}", userId);
    } catch (Exception e) {
        log.error("Failed to update Keycloak user: {}", e.getMessage(), e);
        throw new RuntimeException("Keycloak user update failed");
    }
}


}