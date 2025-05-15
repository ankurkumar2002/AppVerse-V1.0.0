package com.appverse.subscription_service.dto;

import com.appverse.subscription_service.enums.SubscriptionPlanBillingInterval;
import com.appverse.subscription_service.enums.SubscriptionPlanStatus; // If status is updatable
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record SubscriptionPlanRequest(
    @NotBlank(message = "Plan name cannot be blank")
    @Size(max = 150)
    String name,

    String description,

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    BigDecimal price,

    @NotBlank(message = "Currency cannot be blank")
    @Size(min = 3, max = 3)
    String currency,

    @NotNull(message = "Billing interval cannot be null")
    SubscriptionPlanBillingInterval billingInterval,

    @NotNull(message = "Billing interval count cannot be null")
    @Min(value = 1, message = "Billing interval count must be at least 1")
    Integer billingIntervalCount,

    @Min(value = 0, message = "Trial period days must be non-negative")
    Integer trialPeriodDays,

    // Status might be set internally or by admin only during specific operations
    // SubscriptionPlanStatus status,

    String gatewayPlanPriceId, // Optional: ID from payment gateway for this plan/price

    List<String> associatedApplicationIds // List of app IDs this plan grants access to
) {}