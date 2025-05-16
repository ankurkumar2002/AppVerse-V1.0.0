// File: com/appverse/subscription_service/event/payload/SubscriptionInitialPaymentFailedPayload.java
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record SubscriptionInitialPaymentFailedPayload(
    String subscriptionId,
    String userId,
    String planId,
    String paymentTransactionId, // May be null if payment initiation itself failed before getting a txId
    String reason,
    Instant failedAt
) {}