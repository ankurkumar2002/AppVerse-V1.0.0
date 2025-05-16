// === In app-service Project ===
package com.appverse.app_service.event.payload;

import java.time.Instant;

public record CategoryDeletedPayload(
    String id,    // ID of the deleted category
    String name,  // Name for context
    String slug,  // Slug for context
    Instant deletedAt
) {}