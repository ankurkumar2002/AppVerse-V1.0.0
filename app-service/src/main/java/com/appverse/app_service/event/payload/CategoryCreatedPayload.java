// === In app-service Project ===
package com.appverse.app_service.event.payload;

import java.time.Instant;

public record CategoryCreatedPayload(
    String id,
    String name,
    String slug
) {}