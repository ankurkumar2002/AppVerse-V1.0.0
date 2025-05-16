// === In subscription-service Project ===
package com.appverse.subscription_service.event.payload;

import com.appverse.subscription_service.enums.SubscriptionPlanBillingInterval;
import com.appverse.subscription_service.enums.SubscriptionPlanStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SubscriptionPlanCreatedPayload(
    String id,
    String name,
    BigDecimal price,
    String currency,
    SubscriptionPlanBillingInterval billingInterval,
    int billingIntervalCount,
    Integer trialPeriodDays,
    SubscriptionPlanStatus status, // Will be ACTIVE
    String gatewayPlanPriceId,     // Optional
    List<String> associatedApplicationIds,
    Instant createdAt
) {}