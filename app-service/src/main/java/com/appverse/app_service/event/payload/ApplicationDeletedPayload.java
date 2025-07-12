
package com.appverse.app_service.event.payload;

import java.time.Instant;

public record ApplicationDeletedPayload(
    String id,
    String developerId,
    String name,
    Instant deletedAt
) {}
