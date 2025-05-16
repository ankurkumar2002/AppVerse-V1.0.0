// File: com/appverse/subscription_service/event/payload/SubscriptionInitiatedPayload.java
package com.appverse.subscription_service.event.payload;

import com.appverse.subscription_service.enums.UserSubscriptionStatus;
import java.time.Instant;

public record SubscriptionInitiatedPayload(
    String subscriptionId,
    String userId,
    String planId,
    String planName, // For context
    UserSubscriptionStatus status, // e.g., TRIALING, PENDING_INITIAL_PAYMENT
    Instant startDate,         // Tentative or actual start
    Instant trialEndDate,      // Null if no trial
    Instant currentPeriodEndDate,
    boolean autoRenew,
    Instant initiatedAt
) {}