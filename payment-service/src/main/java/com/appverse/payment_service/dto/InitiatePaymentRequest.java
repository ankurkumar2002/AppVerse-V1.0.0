package com.appverse.payment_service.dto;

import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentReferenceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

public record InitiatePaymentRequest(
    @NotBlank(message = "User ID cannot be blank")
    String userId,

    @NotBlank(message = "Reference ID cannot be blank")
    String referenceId, // e.g., Order ID, Subscription ID

    @NotNull(message = "Reference type cannot be null")
    PaymentReferenceType referenceType,

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be positive") // Or based on smallest currency unit
    BigDecimal amount,

    @NotBlank(message = "Currency cannot be blank")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    String currency,

    @NotNull(message = "Payment gateway type cannot be null")
    PaymentGatewayType paymentGateway, // e.g., STRIPE, PAYPAL

    String description, // Optional

    String paymentMethodToken, // Token from frontend if user selected an existing or new card via gateway SDK
    String customerId, // Gateway customer ID if applicable and known

    Map<String, Object> metadata // Optional additional metadata
) {}