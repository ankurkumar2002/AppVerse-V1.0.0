// File: com.appverse.user_service.event/payload/UserRoleChangedPayload.java
package com.appverse.user_service.event.payload;

import com.appverse.user_service.enums.Role;
import java.time.Instant;

public record UserRoleChangedPayload(
    String userId,
    String keycloakUserId,
    Role newRole
) {}