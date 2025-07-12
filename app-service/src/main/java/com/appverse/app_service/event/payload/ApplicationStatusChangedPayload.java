
package com.appverse.app_service.event.payload;

import java.time.Instant;

public record ApplicationStatusChangedPayload(
    String id,
    String oldStatus,
    String newStatus,
    Instant statusChangedAt
) {}
