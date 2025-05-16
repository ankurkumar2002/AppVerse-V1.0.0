// File: com/appverse/subscription_service/event/payload/SubscriptionCancelledByUserPayload.java
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record SubscriptionCancelledByUserPayload(
    String subscriptionId,
    String userId,
    String planId,
    String reason, // Optional reason provided by user
    Instant cancelledAt,
    Instant currentPeriodEndDate // When the subscription will actually become inactive
) {}