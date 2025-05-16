// File: com/appverse/user_service/event/payload/UserCreatedPayload.java
package com.appverse.user_service.event.payload;

import com.appverse.user_service.enums.Role;
import com.appverse.user_service.enums.UserStatus;
import java.time.Instant; // Using Instant for events

public record UserCreatedPayload(
    String userId, // Your local UUID as String
    String keycloakUserId,
    String username,
    String email,
    String firstName,
    String lastName,
    String phone,
    boolean emailVerified,
    Role role,
    UserStatus status
) {}