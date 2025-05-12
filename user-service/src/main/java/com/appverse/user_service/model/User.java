package com.appverse.user_service.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.appverse.user_service.enums.Role;
import com.appverse.user_service.enums.UserStatus;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"keycloak_user_id"}, name = "uk_user_keycloak_id"),
    @UniqueConstraint(columnNames = {"username"}, name = "uk_user_username"),
    @UniqueConstraint(columnNames = {"email"}, name = "uk_user_email")
})
@Data
@NoArgsConstructor // Keep this for JPA
@AllArgsConstructor // Keep this if you need it for other purposes or for the builder to use
@Builder
public class User {

    @Id
    private UUID id;

    @Column(name = "keycloak_user_id", nullable = false, unique = true)
    private String keycloakUserId;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false; // Initialize directly

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE; // Initialize directly

    @Column(name = "deactivated_by_admin", nullable = false)
    private boolean deactivatedByAdmin = false; // Initialize directly (boolean defaults to false anyway)

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        // Defaults for status, emailVerified, deactivatedByAdmin are set at field level
        // or should be set by service logic before persist if not using field init.
        // If status is not set by builder, it will use the field initializer.
        if (this.status == null) { // This check is good if field init isn't used and builder might skip it
            this.status = UserStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}