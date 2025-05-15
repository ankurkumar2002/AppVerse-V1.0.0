    package com.appverse.developer_service.dto;

    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import org.hibernate.validator.constraints.URL;

    import com.appverse.developer_service.enums.DeveloperType;
    import com.appverse.developer_service.enums.Role;

    public record DeveloperRequest(
        @NotBlank(message = "Display name cannot be blank")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        @Size(max = 255)
        String email,

        @URL(message = "Website must be a valid URL")
        @Size(max = 255)
        String website,

        @Size(max = 150)
        String companyName,

        String bio,

        @URL(message = "Logo URL must be a valid URL")
        @Size(max = 255)
        String logoUrl,

        @Size(max = 100)
        String location,

        @NotNull(message = "Developer type cannot be null")
        DeveloperType developerType,

        @NotNull(message = "Role cannot be null") // Add role to the request
        Role role
    ) {}