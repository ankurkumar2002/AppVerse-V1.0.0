// === In subscription-service Project ===
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record UserSubscriptionRenewedPayload(
    String subscriptionId,
    String userId,
    String planId,
    String paymentTransactionId, // ID of the successful renewal payment
    Instant newPeriodStartDate,
    Instant newPeriodEndDate
) {}