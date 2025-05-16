// === In app-service Project ===
package com.appverse.app_service.event.payload;

import java.time.Instant;

public record ApplicationDeletedPayload(
    String id,          // ID of the deleted application
    String developerId, // Developer who owned it (for context)
    String name,        // Name of the app (for context)
    Instant deletedAt   // Could be same as eventTimestamp in EventMetaData
) {}

// Full event:
// public record ApplicationDeletedEvent(EventMetaData metaData, ApplicationDeletedPayload payload) {}