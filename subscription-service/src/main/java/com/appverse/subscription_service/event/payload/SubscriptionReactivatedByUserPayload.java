// File: com/appverse/subscription_service/event/payload/SubscriptionReactivatedByUserPayload.java
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record SubscriptionReactivatedByUserPayload(
    String subscriptionId,
    String userId,
    String planId,
    Instant reactivatedAt
) {}