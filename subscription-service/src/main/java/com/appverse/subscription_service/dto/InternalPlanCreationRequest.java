// === In Subscription Service Project ===
package com.appverse.subscription_service.dto;

// You might need to define SubscriptionPlanBillingInterval enum here if not in a shared lib,
// OR accept billingInterval as String and parse it in the service.
// For now, let's assume it's a String to match what app-service's Feign client sends.
// import com.appverse.subscription_service.enums.SubscriptionPlanBillingInterval;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record InternalPlanCreationRequest(
    @NotBlank(message = "Plan name key cannot be blank")
    @Size(max = 100, message = "Plan name key too long")
    String planNameKey, // A unique key for the plan within the app/developer context

    @NotBlank(message = "Display name cannot be blank")
    @Size(max = 150, message = "Display name too long")
    String displayName,

    @Size(max = 1000, message = "Description too long")
    String description,

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    BigDecimal price,

    @NotBlank(message = "Currency cannot be blank")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    String currency,

    @NotBlank(message = "Billing interval cannot be blank")
    // Accepting as String to match what app-service Feign client sends.
    // SubscriptionServiceImpl will parse this into SubscriptionPlanBillingInterval enum.
    String billingInterval, // e.g., "MONTH", "YEAR"

    @NotNull(message = "Billing interval count cannot be null")
    @Min(value = 1, message = "Billing interval count must be at least 1")
    Integer billingIntervalCount,

    @Min(value = 0, message = "Trial period days must be non-negative")
    Integer trialPeriodDays,

    @NotBlank(message = "Application ID cannot be blank")
    String applicationId, // ID of the application this plan belongs to (from app-service)

    @NotBlank(message = "Developer ID cannot be blank")
    String developerId    // ID of the developer who owns this plan
) {}