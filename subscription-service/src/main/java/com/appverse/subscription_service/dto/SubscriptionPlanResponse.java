package com.appverse.subscription_service.dto;

import com.appverse.subscription_service.enums.SubscriptionPlanBillingInterval;
import com.appverse.subscription_service.enums.SubscriptionPlanStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SubscriptionPlanResponse(
    String id,
    String name,
    String description,
    BigDecimal price,
    String currency,
    SubscriptionPlanBillingInterval billingInterval,
    int billingIntervalCount,
    Integer trialPeriodDays,
    SubscriptionPlanStatus status,
    String gatewayPlanPriceId,
    List<String> associatedApplicationIds,
    Instant createdAt,
    Instant updatedAt
) {}