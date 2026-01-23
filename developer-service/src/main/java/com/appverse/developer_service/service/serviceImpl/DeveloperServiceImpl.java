// === In developer-service Project ===
package com.appverse.developer_service.service.serviceImpl;

import com.appverse.developer_service.client.IdentityClient;
import com.appverse.developer_service.config.CurrentUserProvider;
import com.appverse.developer_service.dto.AssignRoleRequest;
import com.appverse.developer_service.dto.DeveloperRequest;
import com.appverse.developer_service.dto.DeveloperResponse;
import com.appverse.developer_service.dto.IdentityUserResponse;
import com.appverse.developer_service.dto.MessageResponse;
import com.appverse.developer_service.enums.DeveloperStatus;
import com.appverse.developer_service.enums.Role;
import com.appverse.developer_service.event.payload.*;
import com.appverse.developer_service.exception.DuplicateResourceException;
import com.appverse.developer_service.exception.ResourceNotFoundException;
import com.appverse.developer_service.mapper.DeveloperMapper;
import com.appverse.developer_service.model.Developer;
import com.appverse.developer_service.repository.DeveloperRepository;
import com.appverse.developer_service.service.DeveloperService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;
    private final DeveloperMapper developerMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String DEVELOPER_EVENTS_TOPIC = "developer-events";
    private final CurrentUserProvider currentUserProvider;
    private final IdentityClient identityClient;

    @Override
    @Transactional
    public MessageResponse createDeveloper(DeveloperRequest request) {

        IdentityUserResponse me = currentUserProvider.getCurrentUser();

        if (developerRepository.existsByKeycloakUserId(me.id())) {
            throw new DuplicateResourceException("Developer profile already exists");
        }

        

        Developer developer = developerMapper.toEntity(request);
        log.info(me.firstName() + " " + me.lastName());

        // identity-owned fields
        developer.setKeycloakUserId(me.id());
        developer.setUsername(me.username());
        developer.setFirstName(me.firstName());
        developer.setLastName(me.lastName());
        developer.setEmail(me.email());
        developer.setRole(Role.DEVELOPER);

        Developer savedDeveloper = developerRepository.save(developer);

        try {
            identityClient.assignRoles(
                    me.id(),
                    new AssignRoleRequest(List.of("DEVELOPER")));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to assign developer role", ex);
        }

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                DEVELOPER_EVENTS_TOPIC,
                savedDeveloper.getId(),
                new DeveloperProfileCreatedPayload(
                        savedDeveloper.getId(),
                        savedDeveloper.getKeycloakUserId(),
                        savedDeveloper.getUsername(),
                        savedDeveloper.getFirstName(),
                        savedDeveloper.getLastName(),
                        savedDeveloper.getEmail(),
                        savedDeveloper.getDeveloperType(),
                        savedDeveloper.getCompanyName(),
                        savedDeveloper.getCreatedAt()));

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Kafka publish failed for developer {}", savedDeveloper.getId(), ex);
            }
        });

        return new MessageResponse("Developer created successfully", savedDeveloper.getId());
    }

    @Override
    @Transactional
    public MessageResponse updateDeveloper(String developerId, DeveloperRequest request) throws AccessDeniedException {
        IdentityUserResponse me = currentUserProvider.getCurrentUser();

        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with ID: " + me.id()));

        if (developer.getStatus() == DeveloperStatus.DISABLED) {
            throw new IllegalStateException("Disabled developer profile cannot be updated");
        }

        if (!developer.getKeycloakUserId().equals(me.id())) {
            throw new AccessDeniedException("You are not allowed to update this developer profile.");
        }

        developerMapper.updateFromDto(request, developer);

        Developer updatedDeveloper = developerRepository.save(developer);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                DEVELOPER_EVENTS_TOPIC,
                updatedDeveloper.getId(),
                new DeveloperProfileUpdatedPayload(
                        updatedDeveloper.getId(),
                        updatedDeveloper.getKeycloakUserId(),

                        updatedDeveloper.getUsername(),
                        updatedDeveloper.getFirstName(),
                        updatedDeveloper.getLastName(),
                        updatedDeveloper.getEmail(),

                        updatedDeveloper.getDeveloperType(),
                        updatedDeveloper.getWebsite(),
                        updatedDeveloper.getCompanyName(),
                        updatedDeveloper.getBio(),
                        updatedDeveloper.getLogoUrl(),
                        updatedDeveloper.getLocation(),

                        updatedDeveloper.getUpdatedAt()));
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Kafka publish failed for developer {}", updatedDeveloper.getId(), ex);
            }
        });

        return new MessageResponse("Developer updated successfully", updatedDeveloper.getId());
    }

    @Override
    @Transactional
    public void deleteDeveloper(String developerId) throws AccessDeniedException {

        IdentityUserResponse me = currentUserProvider.getCurrentUser();

        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found"));

        if (!developer.getKeycloakUserId().equals(me.id())) {
            throw new AccessDeniedException("Not allowed to delete this developer");
        }

        if (developer.getStatus() == DeveloperStatus.DISABLED) {
            return;
        }

        try {
            identityClient.disableUser(me.id());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to disable user in identity service", ex);
        }

        developer.setStatus(DeveloperStatus.DISABLED);
        developerRepository.save(developer);

        kafkaTemplate.send(
                DEVELOPER_EVENTS_TOPIC,
                developer.getId(),
                new DeveloperProfileDeletedPayload(
                        developer.getId(),
                        developer.getKeycloakUserId(),
                        Instant.now()))
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka publish failed for developer {}", developer.getId(), ex);
                    }
                });
    }

    @Override
    @Transactional(readOnly = true)
    public DeveloperResponse getMyDeveloper() {
        IdentityUserResponse me = currentUserProvider.getCurrentUser();

        Developer developer = developerRepository
                .findByKeycloakUserId(me.id())
                .orElseThrow(() -> new ResourceNotFoundException("Developer profile not found"));

        return developerMapper.toResponse(developer);
    }

}