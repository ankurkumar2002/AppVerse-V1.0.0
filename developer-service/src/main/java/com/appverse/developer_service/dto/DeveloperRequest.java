package com.appverse.developer_service.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL; // Or jakarta equivalent if preferred

import com.appverse.developer_service.enums.DeveloperType;


public record DeveloperRequest(
    @NotBlank(message = "Display name cannot be blank")
    @Size(max = 150)
    String name,

    // Email might be pre-filled from Keycloak during creation.
    // Updating it here might need special handling/verification.
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    String email,

    @URL(message = "Website must be a valid URL")
    @Size(max = 255)
    String website, // Optional

    @Size(max = 150)
    String companyName, // Optional

    String bio, // Optional

    @URL(message = "Logo URL must be a valid URL")
    @Size(max = 255)
    String logoUrl, // Optional

    @Size(max = 100)
    String location, // Optional

    @NotNull(message = "Developer type cannot be null") // User likely selects this initially
    DeveloperType developerType
) {}