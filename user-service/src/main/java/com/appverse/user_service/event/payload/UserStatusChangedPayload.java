// File: com/appverse/user_service/event/payload/UserStatusChangedPayload.java
package com.appverse.user_service.event.payload;

import com.appverse.user_service.enums.UserStatus;
import java.time.Instant;

public record UserStatusChangedPayload(
    String userId,
    String keycloakUserId,
    UserStatus newStatus,
    boolean isAdminAction
) {}