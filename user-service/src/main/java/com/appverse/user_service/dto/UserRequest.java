package com.appverse.user_service.dto;

import com.appverse.user_service.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank; // Good for strings that must have content
import jakarta.validation.constraints.NotNull;  // Good for objects/enums that must be present
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank(message = "Keycloak User ID cannot be blank")
    String keycloakUserId, // Remains essential

    // For username and email, if they are truly optional in the request
    // because you intend to fetch them from Keycloak if not provided,
    // you might remove @NotBlank and handle null/blank in your service.
    // However, if you expect the client to *always* provide them even if they
    // are just echoing what Keycloak has, then @NotBlank is fine.
    // Let's assume for now they are expected in the request.
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    String username,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    String email,

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    String phone, // Optional is good

    @NotNull(message = "Role cannot be null")
    Role role // Role is application-specific, so client provides it
) {}