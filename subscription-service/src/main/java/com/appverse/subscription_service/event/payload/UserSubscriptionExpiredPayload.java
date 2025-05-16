// === In subscription-service Project ===
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record UserSubscriptionExpiredPayload(
    String subscriptionId,
    String userId,
    String planId,
    Instant expiredAt
) {}