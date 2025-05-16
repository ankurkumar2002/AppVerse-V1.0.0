// File: com/appverse/user_service/event/payload/UserProfileUpdatedPayload.java
package com.appverse.user_service.event.payload;

import com.appverse.user_service.enums.Role;
import java.time.Instant;

public record UserProfileUpdatedPayload(
    String userId,
    String keycloakUserId, // For reference
    String newUsername,
    String newEmail,
    String newPhone,
    Role newRole
) {}