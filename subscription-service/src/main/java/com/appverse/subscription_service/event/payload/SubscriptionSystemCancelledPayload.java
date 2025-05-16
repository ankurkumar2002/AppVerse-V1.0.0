// File: com/appverse/subscription_service/event/payload/SubscriptionSystemCancelledPayload.java
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record SubscriptionSystemCancelledPayload(
    String subscriptionId,
    String userId,
    String planId,
    String reason, // e.g., "Associated plan is no longer active"
    Instant cancelledAt
) {}