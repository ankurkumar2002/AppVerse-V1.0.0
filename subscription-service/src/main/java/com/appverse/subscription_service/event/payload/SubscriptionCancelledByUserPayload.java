// File: com/appverse/subscription_service/event/payload/SubscriptionCancelledByUserPayload.java
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record SubscriptionCancelledByUserPayload(
    String subscriptionId,
    String userId,
    String planId,
    String reason, 
    Instant cancelledAt,
    Instant currentPeriodEndDate 
) {}