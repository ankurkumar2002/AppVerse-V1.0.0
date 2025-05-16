// File: com/appverse/subscription_service/event/payload/SubscriptionActivatedPayload.java
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record SubscriptionActivatedPayload(
    String subscriptionId,
    String userId,
    String planId,
    String planName, // For context
    Instant startDate,
    Instant currentPeriodStartDate,
    Instant currentPeriodEndDate,
    String lastSuccessfulPaymentId, // Can be null for free plans or initial trial activation
    String storedPaymentMethodId,   // Can be null
    Instant activatedAt
) {}