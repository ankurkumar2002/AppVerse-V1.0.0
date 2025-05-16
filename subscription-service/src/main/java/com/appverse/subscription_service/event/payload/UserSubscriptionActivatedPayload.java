// === In subscription-service Project ===
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record UserSubscriptionActivatedPayload(
    String subscriptionId,
    String userId,
    String planId,
    String paymentTransactionId, // ID of the payment that activated it (if any)
    String storedPaymentMethodId, // ID of the method used for payment/to be used for renewals
    Instant activeSince,
    Instant currentPeriodStartDate,
    Instant currentPeriodEndDate
) {}