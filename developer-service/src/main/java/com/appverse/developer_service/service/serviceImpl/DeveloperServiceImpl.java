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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult; // <<< IMPORT FOR COMPLETABLEFUTURE
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture; // <<< IMPORT FOR COMPLETABLEFUTURE

@Service
@RequiredArgsConstructor
@Slf4j
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;
    private final DeveloperMapper developerMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String DEVELOPER_EVENTS_TOPIC = "developer-events";

    @Override
    @Transactional
    public MessageResponse createDeveloper(DeveloperRequest request) {
        log.info("Attempting to create developer profile for email: {}", request.email());

        String keycloakUserId = getKeycloakUserIdFromSecurityContext();
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            log.error("Keycloak User ID is missing from security context. Cannot create developer profile.");
            throw new IllegalArgumentException("Keycloak User ID must be provided for developer creation.");
        }
        log.debug("Creating developer for Keycloak User ID: {}", keycloakUserId);

        // if (developerRepository.existsByKeycloakUserId(keycloakUserId)) {
        //     throw new DuplicateResourceException("Developer profile already exists for Keycloak user ID: " + keycloakUserId);
        // }
        // if (developerRepository.existsByEmailIgnoreCase(request.email())) {
        //     throw new DuplicateResourceException("Developer with email '" + request.email() + "' already exists.");
        // }

        Developer developer = developerMapper.toEntity(request);
        developer.setKeycloakUserId(keycloakUserId);

        try {
            Developer savedDeveloper = developerRepository.save(developer);
            log.info("Developer profile created successfully with ID: {} for Keycloak ID: {}",
                     savedDeveloper.getId(), keycloakUserId);

            DeveloperProfileCreatedPayload payload = new DeveloperProfileCreatedPayload(
                    savedDeveloper.getId(),
                    savedDeveloper.getKeycloakUserId(),
                    savedDeveloper.getName(),
                    savedDeveloper.getEmail(),
                    savedDeveloper.getDeveloperType(),
                    savedDeveloper.getCompanyName(),
                    savedDeveloper.getCreatedAt()
            );

            // --- ENHANCED KAFKA SEND WITH LOGGING CALLBACK ---
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(DEVELOPER_EVENTS_TOPIC, savedDeveloper.getId(), payload);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent DeveloperProfileCreatedEvent to topic {} for key {}: offset {}, partition {}",
                            DEVELOPER_EVENTS_TOPIC, savedDeveloper.getId(),
                            result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to send DeveloperProfileCreatedEvent to topic {} for key {}: {}",
                            DEVELOPER_EVENTS_TOPIC, savedDeveloper.getId(), ex.getMessage(), ex);
                    // Consider further error handling here if send failure is critical (e.g., add to a retry queue)
                }
            });
            log.debug("Asynchronously published DeveloperProfileCreatedEvent for Developer ID: {}. Callback will log success/failure.", savedDeveloper.getId());
            // Note: The main thread continues, and the log above appears before the callback log.

            return new MessageResponse("Developer created successfully.", savedDeveloper.getId());
        } catch (DataAccessException e) {
            log.error("Database error creating developer for Keycloak ID {}: {}", keycloakUserId, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to create developer due to a database issue."+ e);
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
            throw new DuplicateResourceException("Cannot update: Another developer with email '" + request.email() + "' already exists.");
        }

        developerMapper.updateFromDto(request, existingDeveloper);

        try {
            Developer updatedDeveloper = developerRepository.save(existingDeveloper);
            log.info("Developer profile updated successfully with ID: {}", id);

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
                    updatedDeveloper.getUpdatedAt()
            );

            // --- ENHANCED KAFKA SEND WITH LOGGING CALLBACK ---
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(DEVELOPER_EVENTS_TOPIC, updatedDeveloper.getId(), payload);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent DeveloperProfileUpdatedEvent to topic {} for key {}: offset {}, partition {}",
                            DEVELOPER_EVENTS_TOPIC, updatedDeveloper.getId(),
                            result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to send DeveloperProfileUpdatedEvent to topic {} for key {}: {}",
                            DEVELOPER_EVENTS_TOPIC, updatedDeveloper.getId(), ex.getMessage(), ex);
                }
            });
            log.debug("Asynchronously published DeveloperProfileUpdatedEvent for Developer ID: {}.", updatedDeveloper.getId());


            return new MessageResponse("Developer updated successfully.", updatedDeveloper.getId());
        } catch (DataAccessException e) {
            log.error("Database error updating developer with ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to update developer due to a database issue."+ e);
        }
    }

    @Override
    @Transactional
    public void deleteDeveloper(String id) {
        log.info("Attempting to delete developer profile with ID: {}", id);
        Developer developerToDelete = developerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with ID: " + id + ", cannot delete."));

        try {
            developerRepository.deleteById(id);
            log.info("Developer profile deleted successfully with ID: {}", id);

            DeveloperProfileDeletedPayload payload = new DeveloperProfileDeletedPayload(
                    developerToDelete.getId(),
                    developerToDelete.getKeycloakUserId(),
                    developerToDelete.getName(),
                    developerToDelete.getEmail(),
                    Instant.now()
            );

            // --- ENHANCED KAFKA SEND WITH LOGGING CALLBACK ---
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(DEVELOPER_EVENTS_TOPIC, developerToDelete.getId(), payload);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent DeveloperProfileDeletedEvent to topic {} for key {}: offset {}, partition {}",
                            DEVELOPER_EVENTS_TOPIC, developerToDelete.getId(),
                            result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to send DeveloperProfileDeletedEvent to topic {} for key {}: {}",
                            DEVELOPER_EVENTS_TOPIC, developerToDelete.getId(), ex.getMessage(), ex);
                }
            });
            log.debug("Asynchronously published DeveloperProfileDeletedEvent for Developer ID: {}.", developerToDelete.getId());

        } catch (DataAccessException e) {
            log.error("Database error deleting developer with ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to delete developer due to a database issue."+ e);
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
            throw new DatabaseOperationException("Failed to retrieve developers due to a database issue."+ e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String id) {
        log.debug("Checking existence for developer ID: {}", id);
        return developerRepository.existsById(id);
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
}