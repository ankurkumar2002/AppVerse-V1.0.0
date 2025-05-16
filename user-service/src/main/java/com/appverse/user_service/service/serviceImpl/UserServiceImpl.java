// === In User Service Project ===
package com.appverse.user_service.service.serviceImpl;

import com.appverse.user_service.dto.UserRequest;
import com.appverse.user_service.dto.UserResponse;
import com.appverse.user_service.enums.Role;
import com.appverse.user_service.enums.UserStatus;
import com.appverse.user_service.event.payload.*; // KAFKA PAYLOAD IMPORTS
import com.appverse.user_service.exception.DatabaseOperationException;
import com.appverse.user_service.exception.DuplicateResourceException;
import com.appverse.user_service.exception.IntegrationException;
import com.appverse.user_service.exception.ResourceNotFoundException;
import com.appverse.user_service.mapper.UserMapper;
import com.appverse.user_service.model.User;
import com.appverse.user_service.repository.UserRepository;
import com.appverse.user_service.service.UserService;

import lombok.RequiredArgsConstructor;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate; // KAFKA IMPORT
import org.springframework.kafka.support.SendResult;   // KAFKA IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.NotFoundException;
import java.time.Instant; // For event payloads
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture; // KAFKA IMPORT

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Keycloak keycloakAdminClient;
    private final KafkaTemplate<String, Object> kafkaTemplate; // KAFKA INJECTION

    @Value("${appverse.keycloak.realm}")
    private String keycloakRealm;

    private static final String USER_EVENTS_TOPIC = "user-events"; // KAFKA TOPIC

    // Centralized Kafka Event Publishing Helper
    private <T> void logKafkaSendAttempt(CompletableFuture<SendResult<String, T>> future, String eventName, String eventKey) {
        log.debug("Submitted {} to Kafka for key {}. Awaiting async result...", eventName, eventKey);
        future.whenComplete((sendResult, exception) -> {
            if (exception == null) {
                log.info("Successfully sent {} to topic {} for key {}: offset {}, partition {}",
                        eventName, USER_EVENTS_TOPIC, eventKey,
                        sendResult.getRecordMetadata().offset(), sendResult.getRecordMetadata().partition());
            } else {
                log.error("Failed to send {} to topic {} for key {}: {}",
                        eventName, USER_EVENTS_TOPIC, eventKey, exception.getMessage(), exception);
            }
        });
    }


    @Override
    public UserResponse createUser(UserRequest userRequest) {
        // if (userRepository.existsByKeycloakUserId(userRequest.keycloakUserId())) {
        //     throw new DuplicateResourceException("User profile with Keycloak ID '" + userRequest.keycloakUserId() + "' already exists locally.");
        // }

        UserRepresentation keycloakUserRep;
        try {
            log.debug("Attempting to fetch user details from Keycloak for ID: {}", userRequest.keycloakUserId());
            UserResource userResource = keycloakAdminClient.realm(keycloakRealm)
                                           .users()
                                           .get(userRequest.keycloakUserId());
            keycloakUserRep = userResource.toRepresentation();
            if (keycloakUserRep == null) {
                 throw new ResourceNotFoundException("User with Keycloak ID '" + userRequest.keycloakUserId() + "' not found in Keycloak (returned null).");
            }
            log.info("Successfully fetched user '{}' from Keycloak (ID: {})", keycloakUserRep.getUsername(), userRequest.keycloakUserId());
        } catch (NotFoundException e) {
            log.warn("User with Keycloak ID {} not found in Keycloak.", userRequest.keycloakUserId(), e);
            throw new ResourceNotFoundException("User to be provisioned (Keycloak ID: '" + userRequest.keycloakUserId() + "') was not found in Keycloak.");
        } catch (Exception e) {
            log.error("Error fetching user from Keycloak (ID {}): {}", userRequest.keycloakUserId(), e.getMessage(), e);
            throw new IntegrationException("Failed to fetch user details from Keycloak for ID '" + userRequest.keycloakUserId() + "'.", e);
        }

        String finalUsername = determineUsername(userRequest, keycloakUserRep);
        String finalEmail = determineEmail(userRequest, keycloakUserRep);
        boolean emailVerified = keycloakUserRep.isEmailVerified() != null && keycloakUserRep.isEmailVerified();
        String firstName = keycloakUserRep.getFirstName();
        String lastName = keycloakUserRep.getLastName();

        if (userRepository.existsByUsername(finalUsername)) {
            throw new DuplicateResourceException("User with username '" + finalUsername + "' already exists locally.");
        }
        if (finalEmail != null && !finalEmail.isBlank() && userRepository.existsByEmail(finalEmail)) {
            throw new DuplicateResourceException("User with email '" + finalEmail + "' already exists locally.");
        }

        User newUser = User.builder()
                .keycloakUserId(userRequest.keycloakUserId())
                .username(finalUsername)
                .email(finalEmail)
                .emailVerified(emailVerified)
                .firstName(firstName)
                .lastName(lastName)
                .phone(userRequest.phone())
                .role(userRequest.role())
                // Assuming @PrePersist in User entity handles: id, createdAt, updatedAt, status
                .build();

        try {
            User savedUser = userRepository.save(newUser);
            log.info("User profile created locally for Keycloak ID {} with local ID: {}", savedUser.getKeycloakUserId(), savedUser.getId());

            // KAFKA: Publish UserCreatedEvent
            UserCreatedPayload payload = new UserCreatedPayload(
                savedUser.getId().toString(), // Convert UUID to String for consistency if needed, or keep UUID
                savedUser.getKeycloakUserId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getPhone(),
                savedUser.isEmailVerified(),
                savedUser.getRole(),
                savedUser.getStatus()
            );
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(USER_EVENTS_TOPIC, savedUser.getId().toString(), payload);
            logKafkaSendAttempt(future, "UserCreatedEvent", savedUser.getId().toString());

            return userMapper.toResponse(savedUser);
        } catch (DataAccessException e) {
            log.error("Database error saving new user profile for Keycloak ID {}: {}", newUser.getKeycloakUserId(), e.getMessage(), e);
            throw new DatabaseOperationException("Failed to save new user profile to the database. " + e.getMessage());
        }
    }

    private String determineUsername(UserRequest request, UserRepresentation kcUser) {
        // ... (your existing logic)
        if (request.username() != null && !request.username().isBlank()) return request.username();
        if (kcUser.getUsername() != null && !kcUser.getUsername().isBlank()) return kcUser.getUsername();
        if (kcUser.getFirstName() != null && !kcUser.getFirstName().isBlank() && kcUser.getLastName() != null && !kcUser.getLastName().isBlank()) {
            String generated = (kcUser.getFirstName() + "_" + kcUser.getLastName()).toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-z0-9_]", "");
            return generated.substring(0, Math.min(generated.length(), 100));
        }
        if (kcUser.getEmail() != null && !kcUser.getEmail().isBlank()) {
            String emailPrefix = kcUser.getEmail().split("@")[0];
            String generated = emailPrefix.replaceAll("[^a-zA-Z0-9_.-]", "");
            return generated.substring(0, Math.min(generated.length(), 100));
        }
        log.warn("Could not determine a suitable username for Keycloak ID {}, generating a placeholder.", kcUser.getId());
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String determineEmail(UserRequest request, UserRepresentation kcUser) {
        // ... (your existing logic)
         if (request.email() != null && !request.email().isBlank()) return request.email();
        return kcUser.getEmail();
    }


    @Override
    public UserResponse updateUserProfile(UUID userId, UserRequest userRequest) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        String newUsername = (userRequest.username() != null && !userRequest.username().isBlank())
                ? userRequest.username() : existingUser.getUsername();
        String newEmail = (userRequest.email() != null && !userRequest.email().isBlank())
                ? userRequest.email() : existingUser.getEmail();

        if (!existingUser.getUsername().equalsIgnoreCase(newUsername) &&
            userRepository.existsByUsername(newUsername)) {
            throw new DuplicateResourceException("Cannot update: Another user with username '" + newUsername + "' already exists.");
        }
        if (newEmail != null && !newEmail.isBlank() && 
            !existingUser.getEmail().equalsIgnoreCase(newEmail) &&
            userRepository.existsByEmail(newEmail)) {
            throw new DuplicateResourceException("Cannot update: Another user with email '" + newEmail + "' already exists.");
        }
        
        UserRequest effectiveUpdateRequest = new UserRequest(
            existingUser.getKeycloakUserId(), newUsername, newEmail,
            userRequest.phone(), userRequest.role()
        );
        userMapper.updateEntityFromRequest(effectiveUpdateRequest, existingUser);
        // Ensure fields like firstName, lastName, emailVerified are NOT updated here
        // unless explicitly allowed and sourced (e.g. from a Keycloak sync).
        // This method primarily updates user-mutable profile fields (username, email, phone, app-specific role).

        try {
            User updatedUser = userRepository.save(existingUser);
            log.info("User profile updated successfully for ID: {}", userId);

            // KAFKA: Publish UserProfileUpdatedEvent
            UserProfileUpdatedPayload payload = new UserProfileUpdatedPayload(
                updatedUser.getId().toString(),
                updatedUser.getKeycloakUserId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getPhone(),
                updatedUser.getRole()
            );
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(USER_EVENTS_TOPIC, updatedUser.getId().toString(), payload);
            logKafkaSendAttempt(future, "UserProfileUpdatedEvent", updatedUser.getId().toString());

            return userMapper.toResponse(updatedUser);
        } catch (DataAccessException e) {
            log.error("Database error updating user profile for ID {}: {}", userId, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to update user profile due to a database issue. "+  e.getMessage());
        }
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId + ", cannot delete."));
        
        UserStatus oldStatus = user.getStatus(); // Capture old status if needed for event later
        user.setStatus(UserStatus.DELETED);
        String suffix = "_deleted_" + UUID.randomUUID().toString().substring(0,4);
        // Anonymize only if not already anonymized to prevent overly long names
        if (!user.getUsername().contains("_deleted_")) {
             user.setUsername(user.getUsername().substring(0, Math.min(user.getUsername().length(), 100 - suffix.length() -1)) + suffix);
        }
        if (user.getEmail() != null && !user.getEmail().contains("_deleted_")) {
            user.setEmail(user.getEmail().substring(0, Math.min(user.getEmail().length(), 150 - suffix.length() -1)) + suffix);
        }
        if (!user.getKeycloakUserId().contains("_deleted_")) {
            user.setKeycloakUserId(user.getKeycloakUserId().substring(0, Math.min(user.getKeycloakUserId().length(), 255 - suffix.length() -1)) + suffix);
        }
        // user.setUpdatedAt(LocalDateTime.now()); // Handled by @LastModifiedDate

        try {
            User deletedUser = userRepository.save(user); 
            log.info("User with ID {} (soft) deleted successfully.", userId);

            // KAFKA: Publish UserDeletedEvent
            UserDeletedPayload payload = new UserDeletedPayload(
                deletedUser.getId().toString(),
                deletedUser.getKeycloakUserId()
            );
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(USER_EVENTS_TOPIC, deletedUser.getId().toString(), payload);
            logKafkaSendAttempt(future, "UserDeletedEvent", deletedUser.getId().toString());

        } catch (DataAccessException e) {
            log.error("Database error (soft) deleting user with ID {}: {}", userId, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to (soft) delete user due to a database issue. "+ e.getMessage());
        }
    }

    @Override
    public UserResponse updateUserStatus(UUID userId, UserStatus newStatus, boolean isAdminAction) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setStatus(newStatus);
        if (isAdminAction && (newStatus == UserStatus.INACTIVE || newStatus == UserStatus.RESTRICTED)) {
            user.setDeactivatedByAdmin(true);
        } else if (newStatus == UserStatus.ACTIVE) {
             user.setDeactivatedByAdmin(false);
        }
        // user.setUpdatedAt(LocalDateTime.now()); // Handled by @LastModifiedDate
        
        try {
            User updatedUser = userRepository.save(user);
            log.info("Status updated to {} for user ID: {}", newStatus, userId);

            // KAFKA: Publish UserStatusChangedEvent
            UserStatusChangedPayload payload = new UserStatusChangedPayload(
                updatedUser.getId().toString(),
                updatedUser.getKeycloakUserId(),
                updatedUser.getStatus(),
                updatedUser.isDeactivatedByAdmin()
            );
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(USER_EVENTS_TOPIC, updatedUser.getId().toString(), payload);
            logKafkaSendAttempt(future, "UserStatusChangedEvent", updatedUser.getId().toString());

            return userMapper.toResponse(updatedUser);
        } catch (DataAccessException e) {
            log.error("Database error updating status for user ID {}: {}", userId, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to update user status due to a database issue. " + e.getMessage());
        }
    }

    @Override
    public UserResponse updateUserRole(UUID userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setRole(newRole);
        // user.setUpdatedAt(LocalDateTime.now()); // Handled by @LastModifiedDate

        try {
            User updatedUser = userRepository.save(user);
            log.info("Role updated to {} for user ID: {}", newRole, userId);

            // KAFKA: Publish UserRoleChangedEvent
            UserRoleChangedPayload payload = new UserRoleChangedPayload(
                updatedUser.getId().toString(),
                updatedUser.getKeycloakUserId(),
                updatedUser.getRole()
            );
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(USER_EVENTS_TOPIC, updatedUser.getId().toString(), payload);
            logKafkaSendAttempt(future, "UserRoleChangedEvent", updatedUser.getId().toString());

            return userMapper.toResponse(updatedUser);
        } catch (DataAccessException e) {
            log.error("Database error updating role for user ID {}: {}", userId, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to update user role due to a database issue. "+ e.getMessage());
        }
    }

    // --- Read-only methods (no Kafka events) & other methods ---
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByKyloakUserId(String keycloakUserId) {
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Keycloak ID: " + keycloakUserId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return userMapper.toResponseList(users);
        } catch (DataAccessException e) {
            log.error("Database error retrieving all users: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Failed to retrieve all users due to a database issue. "+ e.getMessage());
        }
    }
    
    @Override
    public void recordUserLogin(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now()); // This is fine as LocalDateTime
            // user.setUpdatedAt(LocalDateTime.now()); // Handled by @LastModifiedDate
            try {
                userRepository.save(user);
                log.info("Recorded login for user ID: {}", userId);
                // No Kafka event typically published for just a login record update
            } catch (DataAccessException e) {
                log.error("Database error recording login for user ID {}: {}", userId, e.getMessage(), e);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByKeycloakUserId(String keycloakUserId) {
        return userRepository.existsByKeycloakUserId(keycloakUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}