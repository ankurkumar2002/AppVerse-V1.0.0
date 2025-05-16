// === In app-service Project ===
package com.appverse.app_service.event.payload;

import java.time.Instant;

public record CategoryUpdatedPayload(
    String id,
    String name,
    String slug
) {}