// === In subscription-service Project ===
package com.appverse.subscription_service.event.payload;

import com.appverse.subscription_service.enums.SubscriptionPlanStatus;
import java.time.Instant;

public record SubscriptionPlanStatusChangedPayload(
    String id,
    String name,
    SubscriptionPlanStatus oldStatus, // Optional, if you want to include it
    SubscriptionPlanStatus newStatus, // ACTIVE or INACTIVE
    Instant statusChangedAt
) {}