package com.appverse.app_service.dto;



import com.appverse.app_service.enums.SubscriptionPlanBillingInterval; 
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record DeveloperOfferedSubscriptionPlanDto(
    @NotBlank String planNameKey, 
    @NotBlank String displayName,
    String description,
    @NotNull @DecimalMin("0.00") BigDecimal price,
    @NotBlank @Size(min=3, max=3) String currency,
    @NotNull SubscriptionPlanBillingInterval billingInterval,
    @NotNull @Min(1) Integer billingIntervalCount,
    @Min(0) Integer trialPeriodDays
) {}