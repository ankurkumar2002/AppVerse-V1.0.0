package com.appverse.payment_service.dto;

import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentMethodType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.YearMonth;
import java.util.Map;

public record AddStoredPaymentMethodRequest(
    // userId will come from JWT
    @NotNull PaymentGatewayType paymentGateway,
    @NotBlank String gatewayPaymentMethodToken, // This is the token from Stripe Elements, PayPal SDK, etc.
    @NotNull PaymentMethodType type, // CARD, BANK_ACCOUNT
    String gatewayCustomerId, // Optional, if you manage customers at gateway
    String brand, // "Visa", "Mastercard"
    String last4,
    @Min(1) @Max(12) Integer expiryMonth,
    @Min(2024) Integer expiryYear, // Adjust min year as appropriate
    boolean isDefault,
    Map<String, String> billingDetails // e.g., name, address_line1, postal_code, country
) {}