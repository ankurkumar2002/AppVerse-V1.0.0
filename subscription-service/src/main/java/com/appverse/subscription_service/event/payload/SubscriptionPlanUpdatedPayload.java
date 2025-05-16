// === In subscription-service Project ===
package com.appverse.subscription_service.event.payload;

import com.appverse.subscription_service.enums.SubscriptionPlanBillingInterval;
import com.appverse.subscription_service.enums.SubscriptionPlanStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SubscriptionPlanUpdatedPayload(
    String id,
    String name,
    String description, // Example: include all updatable fields
    BigDecimal price,
    String currency,
    SubscriptionPlanBillingInterval billingInterval,
    int billingIntervalCount,
    Integer trialPeriodDays,
    SubscriptionPlanStatus status,
    String gatewayPlanPriceId,
    List<String> associatedApplicationIds,
    Instant updatedAt
) {}