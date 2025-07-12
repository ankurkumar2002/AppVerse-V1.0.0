package com.appverse.app_service.event.payload;

import java.time.Instant;

public record CategoryDeletedPayload(
    String id,
    String name,
    String slug,
    Instant deletedAt
) {}