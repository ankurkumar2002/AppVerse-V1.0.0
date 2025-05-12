package com.appverse.user_service.dto;

import com.appverse.user_service.enums.Role;
import com.appverse.user_service.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String keycloakUserId,
    String username,
    String email,
    boolean emailVerified, // ADDED: This should be included based on the refined entity
    String firstName,     // ADDED: This should be included
    String lastName,      // ADDED: This should be included
    String phone,
    Role role,
    UserStatus status,
    boolean deactivatedByAdmin,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastLoginAt
) {}