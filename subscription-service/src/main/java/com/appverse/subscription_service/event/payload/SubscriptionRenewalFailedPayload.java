// File: com/appverse/subscription_service/event/payload/SubscriptionRenewalFailedPayload.java
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record SubscriptionRenewalFailedPayload(
    String subscriptionId,
    String userId,
    String planId,
    String paymentTransactionId, // May be null if payment initiation failed
    String reason,               // e.g., "Payment declined", "No stored payment method"
    Instant failedAt
) {}