// File: com.appverse.user_service.event/payload/UserDeletedPayload.java
package com.appverse.user_service.event.payload;

import java.time.Instant;

public record UserDeletedPayload(
    String userId,
    String keycloakUserId
) {}