package com.appverse.app_service.dto;



import com.appverse.app_service.enums.SubscriptionPlanBillingInterval; // You'll need this enum in app-service or a shared lib
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record DeveloperOfferedSubscriptionPlanDto(
    @NotBlank String planNameKey, // A key for this plan, e.g., "pro_monthly", "basic_yearly" (unique per app)
    @NotBlank String displayName, // e.g., "Pro Monthly Access"
    String description,
    @NotNull @DecimalMin("0.00") BigDecimal price,
    @NotBlank @Size(min=3, max=3) String currency,
    @NotNull SubscriptionPlanBillingInterval billingInterval,
    @NotNull @Min(1) Integer billingIntervalCount,
    @Min(0) Integer trialPeriodDays
    // No status here, new plans created via app-service for a developer would likely default to ACTIVE in subscription-service
    // No gatewayPlanPriceId here initially, that might be set later by subscription-service if it integrates with gateway for plans
) {}