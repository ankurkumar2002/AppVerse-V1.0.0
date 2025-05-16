// File: com/appverse/subscription_service/event/payload/PlanCreatedPayload.java
package com.appverse.subscription_service.event.payload;

import com.appverse.subscription_service.enums.SubscriptionPlanBillingInterval;
import com.appverse.subscription_service.enums.SubscriptionPlanStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PlanCreatedPayload(
    String planId,
    String name,
    String description,
    BigDecimal price,
    String currency,
    SubscriptionPlanBillingInterval billingInterval,
    int billingIntervalCount,
    Integer trialPeriodDays,
    SubscriptionPlanStatus status,
    String applicationId, // Can be null
    String developerId,   // Can be null
    Instant createdAt
) {}