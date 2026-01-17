// === In User Service Project ===
package com.appverse.user_service.service.serviceImpl;

import com.appverse.user_service.client.IdentityClient;
import com.appverse.user_service.dto.AssignRoleRequest;
import com.appverse.user_service.dto.IdentityUserResponse;
import com.appverse.user_service.dto.UpdateUserProfileRequest;
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

import feign.FeignException;
import lombok.RequiredArgsConstructor;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate; // KAFKA IMPORT
import org.springframework.kafka.support.SendResult; // KAFKA IMPORT
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.NotFoundException;
import java.time.Instant; // For event payloads
import java.time.LocalDateTime;
import java.util.Collections;
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
    // private final Keycloak keycloakAdminClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final IdentityClient identityClient;

    // private String determineUsername(UserRequest request, UserRepresentation
    // kcUser) {
    // // ... (your existing logic)
    // if (request.username() != null && !request.username().isBlank())
    // return request.username();
    // if (kcUser.getUsername() != null && !kcUser.getUsername().isBlank())
    // return kcUser.getUsername();
    // if (kcUser.getFirstName() != null && !kcUser.getFirstName().isBlank() &&
    // kcUser.getLastName() != null
    // && !kcUser.getLastName().isBlank()) {
    // String generated = (kcUser.getFirstName() + "_" +
    // kcUser.getLastName()).toLowerCase()
    // .replaceAll("\\s+", "_").replaceAll("[^a-z0-9_]", "");
    // return generated.substring(0, Math.min(generated.length(), 100));
    // }
    // if (kcUser.getEmail() != null && !kcUser.getEmail().isBlank()) {
    // String emailPrefix = kcUser.getEmail().split("@")[0];
    // String generated = emailPrefix.replaceAll("[^a-zA-Z0-9_.-]", "");
    // return generated.substring(0, Math.min(generated.length(), 100));
    // }
    // log.warn("Could not determine a suitable username for Keycloak ID {},
    // generating a placeholder.",
    // kcUser.getId());
    // return "user_" + UUID.randomUUID().toString().substring(0, 8);
    // }

    private String getCurrentKeycloakUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return jwt.getSubject(); // ✅ sub = keycloak user id
    }

    @Override
    public UserResponse updateUserProfile(UpdateUserProfileRequest userRequest) {
        String keycloakUserId = getCurrentKeycloakUserId();
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        if (userRequest.phone() != null && !userRequest.phone().isBlank()) {
            user.setPhone(userRequest.phone());
        }
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteMyAccount() {
        String keycloakUserId = getCurrentKeycloakUserId();

        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.DEACTIVATED) {
            return;
        }

        user.setStatus(UserStatus.DEACTIVATED);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        try {
            identityClient.disableUser(keycloakUserId);
        } catch (Exception e) {
            // 🔥 CRITICAL: do NOT rollback DB for Keycloak failure
            log.error("Failed to disable Keycloak user {}", keycloakUserId, e);
        }
    }

    @Override
    public UserResponse getMyprofile() {
        String keycloakUserId = getCurrentKeycloakUserId();
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException(keycloakUserId));
        if (user.getStatus() == UserStatus.DEACTIVATED || UserStatus.DELETED == user.getStatus()) {
            return null;
        }
        return userMapper.toResponse(user);
    }

    // private String determineEmail(UserRequest request, UserRepresentation kcUser)
    // {
    // if (request.email() != null && !request.email().isBlank())
    // return request.email();
    // return kcUser.getEmail();
    // }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {

        log.info("🔥 createUser() started");

        // 1. Extract Keycloak userId from JWT
        String keycloakUserId = getCurrentKeycloakUserId();

        // 2. Prevent duplicate signup
        if (userRepository.existsByKeycloakUserId(keycloakUserId)) {
            throw new DuplicateResourceException(
                    "User already exists for this account");
        }

        // 3. Fetch user details from identity-service (Keycloak)
        IdentityUserResponse identityUser;
        try {
            identityUser = identityClient.getUserById(keycloakUserId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException(
                    "Keycloak user not found for id: " + keycloakUserId);
        } catch (FeignException e) {
            throw new IntegrationException(
                    "Failed to communicate with identity-service", e);
        }

        // 4. Basic sanity check (defensive)
        if (identityUser.username() == null || identityUser.email() == null) {
            throw new IntegrationException(
                    "Incomplete user data received from identity-service");
        }

        // 5. Persist user in app database
        User user = User.builder()
                .keycloakUserId(identityUser.id())
                .username(identityUser.username())
                .email(identityUser.email())
                .emailVerified(identityUser.emailVerified())
                .firstName(identityUser.firstName())
                .lastName(identityUser.lastName())
                .phone(userRequest.phone()) // ONLY app-owned field
                .role(Role.USER) // backend-controlled
                .status(UserStatus.ACTIVE)
                .build();

        try {
            User savedUser = userRepository.save(user);
            // Ensure the user is persisted to database before proceeding
            userRepository.flush();

            log.info("✅ User created with id {}", savedUser.getId());

            // 6. Assign USER role in Keycloak via identity-service (after successful DB
            // save)
            try {
                identityClient.assignRoles(
                        keycloakUserId,
                        new AssignRoleRequest(List.of("USER")));
            } catch (FeignException e) {
                log.error("⚠️ Failed to assign role USER to keycloak user {}",
                        keycloakUserId, e);
                // DO NOT throw - user is already created in DB
            }

            return userMapper.toResponse(savedUser);
        } catch (DataAccessException e) {
            log.error("❌ Database error while creating user: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Failed to create user due to database error: " + e.getMessage());
        }

    }

    // private void assignUserRoleToKeycloakUser(String keycloakUserId) {
    // try {
    // UserResource userResource = keycloakAdminClient.realm(keycloakRealm)
    // .users()
    // .get(keycloakUserId);

    // RoleRepresentation userRole = keycloakAdminClient.realm(keycloakRealm)
    // .roles()
    // .get("user")
    // .toRepresentation();

    // List<RoleRepresentation> existingRoles =
    // userResource.roles().realmLevel().listAll();
    // boolean alreadyAssigned = existingRoles.stream()
    // .anyMatch(r -> r.getName().equalsIgnoreCase("user"));

    // if (!alreadyAssigned) {
    // userResource.roles().realmLevel().add(Collections.singletonList(userRole));
    // log.info("Assigned USER role to Keycloak user {}", keycloakUserId);
    // } else {
    // log.info("USER role already assigned to Keycloak user {}", keycloakUserId);
    // }

    // } catch (Exception e) {
    // log.error("Failed to assign USER role to Keycloak user {}: {}",
    // keycloakUserId, e.getMessage(), e);
    // throw new IntegrationException("Could not assign USER role in Keycloak", e);
    // }
    // }

    // @Override
    // public UserResponse updateUserProfile(UUID userId, UserRequest userRequest) {
    // User existingUser = userRepository.findById(userId)
    // .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " +
    // userId));

    // String newUsername = (userRequest.username() != null &&
    // !userRequest.username().isBlank())
    // ? userRequest.username()
    // : existingUser.getUsername();
    // String newEmail = (userRequest.email() != null &&
    // !userRequest.email().isBlank())
    // ? userRequest.email()
    // : existingUser.getEmail();

    // if (!existingUser.getUsername().equalsIgnoreCase(newUsername) &&
    // userRepository.existsByUsername(newUsername)) {
    // throw new DuplicateResourceException(
    // "Cannot update: Another user with username '" + newUsername + "' already
    // exists.");
    // }
    // if (newEmail != null && !newEmail.isBlank() &&
    // !existingUser.getEmail().equalsIgnoreCase(newEmail) &&
    // userRepository.existsByEmail(newEmail)) {
    // throw new DuplicateResourceException(
    // "Cannot update: Another user with email '" + newEmail + "' already exists.");
    // }

    // UserRequest effectiveUpdateRequest = new UserRequest(
    // existingUser.getKeycloakUserId(),
    // newUsername,
    // newEmail,
    // userRequest.phone(),
    // existingUser.getRole() // ✅ keep current role
    // );

    // userMapper.updateEntityFromRequest(effectiveUpdateRequest, existingUser);
    // // Ensure fields like firstName, lastName, emailVerified are NOT updated here
    // // unless explicitly allowed and sourced (e.g. from a Keycloak sync).
    // // This method primarily updates user-mutable profile fields (username,
    // email,
    // // phone, app-specific role).

    // try {
    // User updatedUser = userRepository.save(existingUser);
    // log.info("User profile updated successfully for ID: {}", userId);

    // // KAFKA: Publish UserProfileUpdatedEvent
    // UserProfileUpdatedPayload payload = new UserProfileUpdatedPayload(
    // updatedUser.getId().toString(),
    // updatedUser.getKeycloakUserId(),
    // updatedUser.getUsername(),
    // updatedUser.getEmail(),
    // updatedUser.getPhone());
    // CompletableFuture<SendResult<String, Object>> future =
    // kafkaTemplate.send(USER_EVENTS_TOPIC,
    // updatedUser.getId().toString(), payload);
    // logKafkaSendAttempt(future, "UserProfileUpdatedEvent",
    // updatedUser.getId().toString());

    // return userMapper.toResponse(updatedUser);
    // } catch (DataAccessException e) {
    // log.error("Database error updating user profile for ID {}: {}", userId,
    // e.getMessage(), e);
    // throw new DatabaseOperationException(
    // "Failed to update user profile due to a database issue. " + e.getMessage());
    // }
    // }

    // @Override
    // public void deleteUser(UUID userId) {
    // User user = userRepository.findById(userId)
    // .orElseThrow(
    // () -> new ResourceNotFoundException("User not found with ID: " + userId + ",
    // cannot delete."));

    // UserStatus oldStatus = user.getStatus(); // Capture old status if needed for
    // event later
    // user.setStatus(UserStatus.DELETED);
    // String suffix = "_deleted_" + UUID.randomUUID().toString().substring(0, 4);
    // // Anonymize only if not already anonymized to prevent overly long names
    // if (!user.getUsername().contains("_deleted_")) {
    // user.setUsername(
    // user.getUsername().substring(0, Math.min(user.getUsername().length(), 100 -
    // suffix.length() - 1))
    // + suffix);
    // }
    // if (user.getEmail() != null && !user.getEmail().contains("_deleted_")) {
    // user.setEmail(user.getEmail().substring(0, Math.min(user.getEmail().length(),
    // 150 - suffix.length() - 1))
    // + suffix);
    // }
    // if (!user.getKeycloakUserId().contains("_deleted_")) {
    // user.setKeycloakUserId(user.getKeycloakUserId().substring(0,
    // Math.min(user.getKeycloakUserId().length(), 255 - suffix.length() - 1)) +
    // suffix);
    // }
    // // user.setUpdatedAt(LocalDateTime.now()); // Handled by @LastModifiedDate

    // try {
    // User deletedUser = userRepository.save(user);
    // log.info("User with ID {} (soft) deleted successfully.", userId);

    // // KAFKA: Publish UserDeletedEvent
    // UserDeletedPayload payload = new UserDeletedPayload(
    // deletedUser.getId().toString(),
    // deletedUser.getKeycloakUserId());
    // CompletableFuture<SendResult<String, Object>> future =
    // kafkaTemplate.send(USER_EVENTS_TOPIC,
    // deletedUser.getId().toString(), payload);
    // logKafkaSendAttempt(future, "UserDeletedEvent",
    // deletedUser.getId().toString());

    // } catch (DataAccessException e) {
    // log.error("Database error (soft) deleting user with ID {}: {}", userId,
    // e.getMessage(), e);
    // throw new DatabaseOperationException(
    // "Failed to (soft) delete user due to a database issue. " + e.getMessage());
    // }
    // }

    // @Override
    // public UserResponse updateUserStatus(UUID userId, UserStatus newStatus,
    // boolean isAdminAction) {
    // User user = userRepository.findById(userId)
    // .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " +
    // userId));

    // user.setStatus(newStatus);
    // if (isAdminAction && (newStatus == UserStatus.INACTIVE || newStatus ==
    // UserStatus.RESTRICTED)) {
    // user.setDeactivatedByAdmin(true);
    // } else if (newStatus == UserStatus.ACTIVE) {
    // user.setDeactivatedByAdmin(false);
    // }
    // // user.setUpdatedAt(LocalDateTime.now()); // Handled by @LastModifiedDate

    // try {
    // User updatedUser = userRepository.save(user);
    // log.info("Status updated to {} for user ID: {}", newStatus, userId);

    // // KAFKA: Publish UserStatusChangedEvent
    // UserStatusChangedPayload payload = new UserStatusChangedPayload(
    // updatedUser.getId().toString(),
    // updatedUser.getKeycloakUserId(),
    // updatedUser.getStatus(),
    // updatedUser.isDeactivatedByAdmin());
    // CompletableFuture<SendResult<String, Object>> future =
    // kafkaTemplate.send(USER_EVENTS_TOPIC,
    // updatedUser.getId().toString(), payload);
    // logKafkaSendAttempt(future, "UserStatusChangedEvent",
    // updatedUser.getId().toString());

    // return userMapper.toResponse(updatedUser);
    // } catch (DataAccessException e) {
    // log.error("Database error updating status for user ID {}: {}", userId,
    // e.getMessage(), e);
    // throw new DatabaseOperationException(
    // "Failed to update user status due to a database issue. " + e.getMessage());
    // }
    // }

    // @Override
    // public UserResponse updateUserRole(UUID userId, Role newRole) {
    // User user = userRepository.findById(userId)
    // .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " +
    // userId));
    // user.setRole(newRole);
    // // user.setUpdatedAt(LocalDateTime.now()); // Handled by @LastModifiedDate

    // try {
    // User updatedUser = userRepository.save(user);
    // log.info("Role updated to {} for user ID: {}", newRole, userId);

    // // KAFKA: Publish UserRoleChangedEvent
    // UserRoleChangedPayload payload = new UserRoleChangedPayload(
    // updatedUser.getId().toString(),
    // updatedUser.getKeycloakUserId(),
    // updatedUser.getRole());
    // CompletableFuture<SendResult<String, Object>> future =
    // kafkaTemplate.send(USER_EVENTS_TOPIC,
    // updatedUser.getId().toString(), payload);
    // logKafkaSendAttempt(future, "UserRoleChangedEvent",
    // updatedUser.getId().toString());

    // return userMapper.toResponse(updatedUser);
    // } catch (DataAccessException e) {
    // log.error("Database error updating role for user ID {}: {}", userId,
    // e.getMessage(), e);
    // throw new DatabaseOperationException(
    // "Failed to update user role due to a database issue. " + e.getMessage());
    // }
    // }

    // // --- Read-only methods (no Kafka events) & other methods ---
    // @Override
    // @Transactional(readOnly = true)
    // public UserResponse getUserById(UUID userId) {
    // User user = userRepository.findById(userId)
    // .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " +
    // userId));
    // return userMapper.toResponse(user);
    // }

    // @Override
    // @Transactional(readOnly = true)
    // public UserResponse getUserByKyloakUserId(String keycloakUserId) {
    // User user = userRepository.findByKeycloakUserId(keycloakUserId)
    // .orElseThrow(() -> new ResourceNotFoundException("User not found with
    // Keycloak ID: " + keycloakUserId));
    // return userMapper.toResponse(user);
    // }

    // @Override
    // @Transactional(readOnly = true)
    // public UserResponse getUserByUsername(String username) {
    // User user = userRepository.findByUsername(username)
    // .orElseThrow(() -> new ResourceNotFoundException("User not found with
    // username: " + username));
    // return userMapper.toResponse(user);
    // }

    // @Override
    // @Transactional(readOnly = true)
    // public UserResponse getUserByEmail(String email) {
    // User user = userRepository.findByEmail(email)
    // .orElseThrow(() -> new ResourceNotFoundException("User not found with email:
    // " + email));
    // return userMapper.toResponse(user);
    // }

    // @Override
    // @Transactional(readOnly = true)
    // public List<UserResponse> getAllUsers() {
    // try {
    // List<User> users = userRepository.findAll();
    // return userMapper.toResponseList(users);
    // } catch (DataAccessException e) {
    // log.error("Database error retrieving all users: {}", e.getMessage(), e);
    // throw new DatabaseOperationException(
    // "Failed to retrieve all users due to a database issue. " + e.getMessage());
    // }
    // }

    // @Override
    // public void recordUserLogin(UUID userId) {
    // userRepository.findById(userId).ifPresent(user -> {
    // user.setLastLoginAt(LocalDateTime.now()); // This is fine as LocalDateTime
    // // user.setUpdatedAt(LocalDateTime.now()); // Handled by @LastModifiedDate
    // try {
    // userRepository.save(user);
    // log.info("Recorded login for user ID: {}", userId);
    // // No Kafka event typically published for just a login record update
    // } catch (DataAccessException e) {
    // log.error("Database error recording login for user ID {}: {}", userId,
    // e.getMessage(), e);
    // }
    // });
    // }

    // @Override
    // @Transactional(readOnly = true)
    // public boolean existsById(UUID userId) {
    // return userRepository.existsById(userId);
    // }

    // @Override
    // @Transactional(readOnly = true)
    // public boolean existsByKeycloakUserId(String keycloakUserId) {
    // return userRepository.existsByKeycloakUserId(keycloakUserId);
    // }

    // @Override
    // @Transactional(readOnly = true)
    // public boolean existsByUsername(String username) {
    // return userRepository.existsByUsername(username);
    // }

    // @Override
    // @Transactional(readOnly = true)
    // public boolean existsByEmail(String email) {
    // return userRepository.existsByEmail(email);
    // }

    // public void assignRoleToKeycloakUser(String keycloakUserId, Role role) {
    // try {
    // UserResource userResource = keycloakAdminClient.realm(keycloakRealm)
    // .users()
    // .get(keycloakUserId);

    // RoleRepresentation roleRep = keycloakAdminClient.realm(keycloakRealm)
    // .roles()
    // .get(role.name().toLowerCase()) // assuming role names match exactly in
    // Keycloak
    // .toRepresentation();

    // if (roleRep == null) {
    // throw new RuntimeException("Role " + role.name() + " not found in
    // Keycloak.");
    // }

    // userResource.roles()
    // .realmLevel()
    // .add(Collections.singletonList(roleRep));

    // log.info("Successfully assigned role {} to Keycloak user {}", role.name(),
    // keycloakUserId);
    // } catch (Exception e) {
    // log.error("Failed to assign role {} to Keycloak user {}: {}", role.name(),
    // keycloakUserId, e.getMessage(),
    // e);
    // throw new IntegrationException("Failed to assign role in Keycloak", e);
    // }
    // }

}