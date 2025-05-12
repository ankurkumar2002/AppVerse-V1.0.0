package com.appverse.user_service.service.serviceImpl;

import com.appverse.user_service.dto.UserRequest;
import com.appverse.user_service.dto.UserResponse;
import com.appverse.user_service.enums.Role;
import com.appverse.user_service.enums.UserStatus;
import com.appverse.user_service.exception.DatabaseOperationException;
import com.appverse.user_service.exception.DuplicateResourceException;
import com.appverse.user_service.exception.IntegrationException;
import com.appverse.user_service.exception.ResourceNotFoundException;
import com.appverse.user_service.mapper.UserMapper;
import com.appverse.user_service.model.User;
import com.appverse.user_service.repository.UserRepository;
import com.appverse.user_service.service.UserService;

import lombok.RequiredArgsConstructor;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.NotFoundException; // JAX-RS NotFoundException
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor // This will create a constructor for all final fields
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Keycloak keycloakAdminClient; // Injected via @RequiredArgsConstructor

    @Value("${appverse.keycloak.realm}")
    private String keycloakRealm;

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        // 1. PRE-CHECK (Local DB): Ensure this Keycloak user isn't already locally provisioned
        // KeycloakPrincipal<?> principal = (KeycloakPrincipal<?>) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // String keycloakUserId = principal.getKeycloakSecurityContext().getToken().getSubject(); // This is the UUID
        // System.out.println(keycloakUserId);

        if (userRepository.existsByKeycloakUserId(userRequest.keycloakUserId())) {
            throw new DuplicateResourceException("User profile with Keycloak ID '" + userRequest.keycloakUserId() + "' already exists locally.");
        }

        // 2. FETCH USER DETAILS FROM KEYCLOAK
        UserRepresentation keycloakUserRep;
        try {
            log.debug("Attempting to fetch user details from Keycloak for ID: {}", userRequest.keycloakUserId());
            UserResource userResource = keycloakAdminClient.realm(keycloakRealm)
                                           .users()
                                           .get(userRequest.keycloakUserId());
            keycloakUserRep = userResource.toRepresentation(); // This can throw NotFoundException if user doesn't exist
            if (keycloakUserRep == null) { // Defensive check, though NotFoundException should cover it
                 throw new ResourceNotFoundException("User with Keycloak ID '" + userRequest.keycloakUserId() + "' not found in Keycloak (returned null).");
            }
            log.info("Successfully fetched user '{}' from Keycloak (ID: {})", keycloakUserRep.getUsername(), userRequest.keycloakUserId());
        } catch (NotFoundException e) { // Specifically catch JAX-RS NotFoundException
            log.warn("User with Keycloak ID {} not found in Keycloak.", userRequest.keycloakUserId(), e);
            throw new ResourceNotFoundException("User to be provisioned (Keycloak ID: '" + userRequest.keycloakUserId() + "') was not found in Keycloak.");
        } catch (Exception e) { // Catch other potential errors from Keycloak client
            log.error("Error fetching user from Keycloak (ID {}): {}", userRequest.keycloakUserId(), e.getMessage(), e);
            throw new IntegrationException("Failed to fetch user details from Keycloak for ID '" + userRequest.keycloakUserId() + "'.", e);
        }

        // 3. DETERMINE FINAL VALUES FOR YOUR LOCAL USER ENTITY
        String finalUsername = determineUsername(userRequest, keycloakUserRep);
        String finalEmail = determineEmail(userRequest, keycloakUserRep);
        boolean emailVerified = keycloakUserRep.isEmailVerified() != null && keycloakUserRep.isEmailVerified();
        String firstName = keycloakUserRep.getFirstName();
        String lastName = keycloakUserRep.getLastName();
        // Consider fetching phone from Keycloak attributes if your UserRequest.phone() is null and you store it there

        // 4. POST-FETCH CHECKS (Local DB): Prevent duplicates with determined/fetched values
        if (userRepository.existsByUsername(finalUsername)) {
            throw new DuplicateResourceException("User with username '" + finalUsername + "' already exists locally.");
        }
        // Ensure email is not null and not blank before checking for duplicates
        if (finalEmail != null && !finalEmail.isBlank() && userRepository.existsByEmail(finalEmail)) {
            throw new DuplicateResourceException("User with email '" + finalEmail + "' already exists locally.");
        }

        // 5. CREATE AND POPULATE YOUR LOCAL `User` ENTITY
        User newUser = User.builder()
                .keycloakUserId(userRequest.keycloakUserId()) // Validated against Keycloak
                .username(finalUsername)
                .email(finalEmail)
                .emailVerified(emailVerified)
                .firstName(firstName)
                .lastName(lastName)
                .phone(userRequest.phone())   // From request, or could be fetched from KC attributes
                .role(userRequest.role())     // From request, as this is application-specific
                // @PrePersist in User entity will handle: id, createdAt, updatedAt, default status, default deactivatedByAdmin
                .build();

        // 6. SAVE TO MYSQL DATABASE
        try {
            User savedUser = userRepository.save(newUser);
            log.info("User profile created locally for Keycloak ID {} with local ID: {}", savedUser.getKeycloakUserId(), savedUser.getId());
            return userMapper.toResponse(savedUser);
        } catch (DataAccessException e) { // Catch Spring's generic data access exception
            log.error("Database error saving new user profile for Keycloak ID {}: {}", newUser.getKeycloakUserId(), e.getMessage(), e);
            throw new DatabaseOperationException("Failed to save new user profile to the database. " + e.getMessage());
        }
    }

    private String determineUsername(UserRequest request, UserRepresentation kcUser) {
        if (request.username() != null && !request.username().isBlank()) {
            return request.username();
        }
        if (kcUser.getUsername() != null && !kcUser.getUsername().isBlank()) {
            return kcUser.getUsername();
        }
        // Fallback logic: try to construct from first/last name
        if (kcUser.getFirstName() != null && !kcUser.getFirstName().isBlank() && 
            kcUser.getLastName() != null && !kcUser.getLastName().isBlank()) {
            String generated = (kcUser.getFirstName() + "_" + kcUser.getLastName()).toLowerCase()
                               .replaceAll("\\s+", "_") // Replace spaces with underscore
                               .replaceAll("[^a-z0-9_]", ""); // Remove non-alphanumeric (except underscore)
            return generated.substring(0, Math.min(generated.length(), 100)); // Ensure length constraint
        }
        // Fallback to email prefix
        if (kcUser.getEmail() != null && !kcUser.getEmail().isBlank()) {
            String emailPrefix = kcUser.getEmail().split("@")[0];
            String generated = emailPrefix.replaceAll("[^a-zA-Z0-9_.-]", ""); // Allow dot and hyphen too
            return generated.substring(0, Math.min(generated.length(), 100));
        }
        log.warn("Could not determine a suitable username for Keycloak ID {}, generating a placeholder.", kcUser.getId());
        return "user_" + UUID.randomUUID().toString().substring(0, 8); // Last resort placeholder
    }

    private String determineEmail(UserRequest request, UserRepresentation kcUser) {
        if (request.email() != null && !request.email().isBlank()) {
            // Optional: Add validation here if request.email() MUST match kcUser.getEmail()
            return request.email();
        }
        return kcUser.getEmail(); // This can be null if user in Keycloak has no email
    }

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
    public UserResponse updateUserProfile(UUID userId, UserRequest userRequest) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Determine new username and email, prioritizing request.
        // For updates, Keycloak User ID from request is typically ignored or used for verification only.
        // The link (existingUser.getKeycloakUserId()) should not change.
        String newUsername = (userRequest.username() != null && !userRequest.username().isBlank())
                ? userRequest.username() : existingUser.getUsername();
        String newEmail = (userRequest.email() != null && !userRequest.email().isBlank())
                ? userRequest.email() : existingUser.getEmail();

        if (!existingUser.getUsername().equalsIgnoreCase(newUsername) &&
            userRepository.existsByUsername(newUsername)) {
            throw new DuplicateResourceException("Cannot update: Another user with username '" + newUsername + "' already exists.");
        }
        if (newEmail != null && !newEmail.isBlank() && 
            !existingUser.getEmail().equalsIgnoreCase(newEmail) && // Only check if email actually changed
            userRepository.existsByEmail(newEmail)) {
            throw new DuplicateResourceException("Cannot update: Another user with email '" + newEmail + "' already exists.");
        }
        
        // Reconstruct a UserRequest for the mapper, preserving immutable Keycloak ID
        // This approach is if your mapper's updateEntityFromRequest expects a UserRequest.
        // Alternatively, directly set fields on existingUser before save.
        UserRequest effectiveUpdateRequest = new UserRequest(
            existingUser.getKeycloakUserId(), // Use existing KC ID
            newUsername,
            newEmail,
            userRequest.phone(),
            userRequest.role()
        );
        userMapper.updateEntityFromRequest(effectiveUpdateRequest, existingUser);
        // Note: userMapper.updateEntityFromRequest should be configured to ignore keycloakUserId,
        // and system-managed fields like createdAt, emailVerified, firstName, lastName (if those
        // are only synced from Keycloak on creation or via a separate sync process).

        try {
            User updatedUser = userRepository.save(existingUser);
            log.info("User profile updated successfully for ID: {}", userId);
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
        
        user.setStatus(UserStatus.DELETED);
        // Anonymize PII for soft delete if unique constraints exist on these fields
        String suffix = "_deleted_" + UUID.randomUUID().toString().substring(0,4); // Shorter suffix
        user.setUsername(user.getUsername().substring(0, Math.min(user.getUsername().length(), 100 - suffix.length() -1)) + suffix); 
        user.setEmail(user.getEmail().substring(0, Math.min(user.getEmail().length(), 150 - suffix.length() -1)) + suffix); 
        user.setKeycloakUserId(user.getKeycloakUserId().substring(0, Math.min(user.getKeycloakUserId().length(), 255 - suffix.length() -1)) + suffix); 

        try {
            userRepository.save(user); 
            log.info("User with ID {} (soft) deleted successfully.", userId);
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
        
        try {
            User updatedUser = userRepository.save(user);
            log.info("Status updated to {} for user ID: {}", newStatus, userId);
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
        try {
            User updatedUser = userRepository.save(user);
            log.info("Role updated to {} for user ID: {}", newRole, userId);
            return userMapper.toResponse(updatedUser);
        } catch (DataAccessException e) {
            log.error("Database error updating role for user ID {}: {}", userId, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to update user role due to a database issue. "+ e.getMessage());
        }
    }

    @Override
    public void recordUserLogin(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            try {
                userRepository.save(user);
                log.info("Recorded login for user ID: {}", userId);
            } catch (DataAccessException e) {
                log.error("Database error recording login for user ID {}: {}", userId, e.getMessage(), e);
                // Not re-throwing as it might not be critical to fail the whole operation
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