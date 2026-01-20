// === In developer-service Project ===
package com.appverse.developer_service.event.payload;

import java.time.Instant;

public record DeveloperProfileDeletedPayload(
    String developerId,    // ID of the deleted developer profile
    String keycloakUserId,           // Email for context
    Instant deletedAt
) {}

    