package com.appverse.developer_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // Needed for auditing

import com.appverse.developer_service.enums.DeveloperStatus;
import com.appverse.developer_service.enums.DeveloperType;
import com.appverse.developer_service.enums.Role;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "developers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"keycloakUserId"}, name = "uk_developer_keycloak_id"), // Ensure one profile per Keycloak user
    @UniqueConstraint(columnNames = {"email"}, name = "uk_developer_email") // Ensure email is unique if storing it here
})
@EntityListeners(AuditingEntityListener.class) // Enable JPA Auditing listeners
public class Developer {

    @Id
    @GeneratedValue(generator = "uuid") // Use a named generator
    @UuidGenerator(style = UuidGenerator.Style.RANDOM) // Hibernate 6+ specific for random UUIDs
    @Column(name = "id", updatable = false, nullable = false, length = 36) // Standard UUID length
    private String id; // Changed to String

    @NotBlank(message = "Keycloak User ID cannot be blank")
    @Column(nullable = false, unique = true) // MUST be unique and linked to Keycloak's ID
    private String keycloakUserId; // Stores the unique ID from Keycloak (e.g., the JWT 'sub')

    @NotBlank(message = "Display name cannot be blank")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name; // Platform-specific display name (can default from Keycloak initially)

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Size(max = 255)
    @Column(length = 255)
    private String website;

    @Size(max = 150)
    @Column(length = 150)
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Size(max = 255)
    @Column(length = 255)
    private String logoUrl;

    @Size(max = 100)
    @Column(length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DeveloperStatus status = DeveloperStatus.PENDING_VERIFICATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DeveloperType developerType = DeveloperType.INDIVIDUAL;

    @Column(nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role; // Add role here to differentiate between USER, DEVELOPER, and ADMIN

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

}
