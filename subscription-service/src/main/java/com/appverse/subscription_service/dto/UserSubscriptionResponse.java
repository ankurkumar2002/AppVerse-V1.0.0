package com.appverse.subscription_service.dto;

import com.appverse.subscription_service.enums.UserSubscriptionStatus;
import java.time.Instant;

public record UserSubscriptionResponse(
    String id,
    String userId,
    String subscriptionPlanId,
    // You might add more plan details here by fetching SubscriptionPlan and mapping
    // String planName,
    UserSubscriptionStatus status,
    Instant startDate,
    Instant endDate, // Nullable
    Instant currentPeriodStartDate,
    Instant currentPeriodEndDate,
    Instant trialEndDate, // Nullable
    Instant cancelledAt, // Nullable
    boolean autoRenew,
    String storedPaymentMethodId, // Nullable
    String gatewaySubscriptionId // Nullable
    // Potentially last payment details if needed
) {}