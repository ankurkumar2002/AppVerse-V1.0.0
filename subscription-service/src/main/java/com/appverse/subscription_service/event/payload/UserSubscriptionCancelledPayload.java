// === In subscription-service Project ===
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record UserSubscriptionCancelledPayload(
    String subscriptionId,
    String userId,
    String planId,
    Instant cancelledAt,
    Instant currentPeriodEndDate, // When it will effectively expire
    String reason
) {}