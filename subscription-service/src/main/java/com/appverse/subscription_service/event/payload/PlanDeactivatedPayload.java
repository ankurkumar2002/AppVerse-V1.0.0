// File: com/appverse/subscription_service/event/payload/PlanDeactivatedPayload.java
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record PlanDeactivatedPayload(
    String planId,
    String planName, // For context
    Instant deactivatedAt
) {}