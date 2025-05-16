// === In Subscription Service Project ===
// File: com/appverse/subscription_service/event/payload/SubscriptionRenewalSuccessfulPayload.java
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

/**
 * Payload for an event indicating a successful subscription renewal.
 */
public record SubscriptionRenewalSuccessfulPayload(
    String subscriptionId,
    String userId,
    String planId,
    Instant newPeriodStartDate,
    Instant newPeriodEndDate,
    String paymentTransactionId, // The ID of the successful payment transaction for this renewal
    Instant renewedAt             // Timestamp when the renewal was processed/confirmed
) {
}