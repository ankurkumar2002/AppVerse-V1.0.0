// File: com/appverse/subscription_service/event/payload/PlanActivatedPayload.java
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record PlanActivatedPayload(
    String planId,
    String planName, // For context
    Instant activatedAt
) {}