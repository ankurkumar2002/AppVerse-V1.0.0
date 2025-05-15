package com.appverse.subscription_service.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserSubscriptionRequest(
    // userId will come from JWT in controller
    @NotBlank(message = "Subscription Plan ID cannot be blank")
    String subscriptionPlanId,

    String paymentMethodToken, // Optional: if providing a new payment method token from frontend (e.g., Stripe pm_xxx)
    String storedPaymentMethodId // Optional: if using an existing stored payment method ID from payment-service
) {}