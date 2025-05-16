// === In subscription-service Project ===
package com.appverse.subscription_service.event.payload;

import com.appverse.subscription_service.enums.UserSubscriptionStatus;
import java.time.Instant;

public record UserSubscriptionCreatedPayload(
    String subscriptionId,
    String userId,
    String planId,
    UserSubscriptionStatus initialStatus, // e.g., PENDING_INITIAL_PAYMENT, TRIALING
    Instant startDate,
    Instant currentPeriodEndDate,
    Instant trialEndDate, // Nullable
    boolean autoRenew
) {}