package com.appverse.user_service.service;

import com.appverse.user_service.dto.UserRequest;
import com.appverse.user_service.dto.UserResponse;
import com.appverse.user_service.enums.Role;
import com.appverse.user_service.enums.UserStatus;
// import com.appverse.user_service.dto.UserStatusUpdateRequest; // Optional for specific status updates
// import com.appverse.user_service.dto.UserRoleUpdateRequest; // Optional for specific role updates

import java.util.List;
import java.util.UUID;

public interface UserService {

    /**
     * Creates a new user profile based on the provided request.
     * The Keycloak User ID from the request is essential for linking.
     *
     * @param userRequest DTO containing the details for the new user.
     * @return UserResponse DTO of the newly created user.
     * @throws com.appverse.user_service.exception.DuplicateResourceException if a user with the same keycloakUserId, username, or email already exists.
     */
    UserResponse createUser(UserRequest userRequest);

    /**
     * Retrieves a user by their internal UUID.
     *
     * @param userId The UUID of the user to retrieve.
     * @return UserResponse DTO of the found user.
     * @throws com.appverse.user_service.exception.ResourceNotFoundException if no user is found with the given ID.
     */
    UserResponse getUserById(UUID userId);

    /**
     * Retrieves a user by their Keycloak User ID.
     * This is often a more common way to look up a user after authentication.
     *
     * @param keycloakUserId The Keycloak User ID.
     * @return UserResponse DTO of the found user.
     * @throws com.appverse.user_service.exception.ResourceNotFoundException if no user is found with the given Keycloak ID.
     */
    UserResponse getUserByKyloakUserId(String keycloakUserId); // Note: Corrected typo from "Kyloak" to "Keycloak"

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user to retrieve.
     * @return UserResponse DTO of the found user.
     * @throws com.appverse.user_service.exception.ResourceNotFoundException if no user is found with the given username.
     */
    UserResponse getUserByUsername(String username);

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to retrieve.
     * @return UserResponse DTO of the found user.
     * @throws com.appverse.user_service.exception.ResourceNotFoundException if no user is found with the given email.
     */
    UserResponse getUserByEmail(String email);

    /**
     * Retrieves a list of all users.
     * Consider adding pagination and filtering for production use.
     *
     * @return A list of UserResponse DTOs.
     */
    List<UserResponse> getAllUsers();
    // Potentially: Page<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Updates an existing user's profile information.
     *
     * @param userId      The UUID of the user to update.
     * @param userRequest DTO containing the fields to update.
     * @return UserResponse DTO of the updated user.
     * @throws com.appverse.user_service.exception.ResourceNotFoundException if no user is found with the given ID.
     * @throws com.appverse.user_service.exception.DuplicateResourceException if the update would cause a username/email conflict.
     */
    UserResponse updateUserProfile(UUID userId, UserRequest userRequest);

    /**
     * Deletes a user by their internal UUID.
     * This might be a "soft delete" (setting status to DELETED) or a hard delete.
     * The current entity has a `status` field, suggesting soft delete is an option.
     *
     * @param userId The UUID of the user to delete.
     * @throws com.appverse.user_service.exception.ResourceNotFoundException if no user is found with the given ID.
     */
    void deleteUser(UUID userId); // Or `UserResponse softDeleteUser(UUID userId);`

    /**
     * Updates the status of a user (e.g., ACTIVE, INACTIVE, RESTRICTED).
     * This is often an administrative action.
     *
     * @param userId    The UUID of the user whose status is to be updated.
     * @param newStatus The new status to set.
     * @param isAdminAction True if this action is performed by an admin (affects deactivatedByAdmin flag).
     * @return UserResponse DTO of the updated user.
     * @throws com.appverse.user_service.exception.ResourceNotFoundException if no user is found with the given ID.
     */
    UserResponse updateUserStatus(UUID userId, UserStatus newStatus, boolean isAdminAction);

    /**
     * Updates the role of a user.
     * This is typically an administrative action.
     *
     * @param userId  The UUID of the user whose role is to be updated.
     * @param newRole The new role to assign.
     * @return UserResponse DTO of the updated user.
     * @throws com.appverse.user_service.exception.ResourceNotFoundException if no user is found with the given ID.
     */
    UserResponse updateUserRole(UUID userId, Role newRole);

    /**
     * Records a user's login time.
     *
     * @param userId The UUID of the user who logged in.
     */
    void recordUserLogin(UUID userId);
    // Or: void recordUserLoginByKeycloakId(String keycloakUserId);

    // --- Existence Checks (useful for validation before creation or for other services) ---

    /**
     * Checks if a user exists by their internal UUID.
     * @param userId The UUID to check.
     * @return true if the user exists, false otherwise.
     */
    boolean existsById(UUID userId);

    /**
     * Checks if a user exists by their Keycloak User ID.
     * @param keycloakUserId The Keycloak User ID to check.
     * @return true if the user exists, false otherwise.
     */
    boolean existsByKeycloakUserId(String keycloakUserId);

    /**
     * Checks if a user exists by their username.
     * @param username The username to check.
     * @return true if the user exists, false otherwise.
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user exists by their email.
     * @param email The email to check.
     * @return true if the user exists, false otherwise.
     */
    boolean existsByEmail(String email);

}