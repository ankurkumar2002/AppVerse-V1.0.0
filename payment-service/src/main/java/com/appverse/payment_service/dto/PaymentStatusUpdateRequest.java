package com.appverse.payment_service.dto;

import com.appverse.payment_service.enums.PaymentMethodType;
import com.appverse.payment_service.enums.PaymentTransactionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record PaymentStatusUpdateRequest(
    // Could identify by gatewayTransactionId or your internal paymentTransactionId
    @NotBlank String paymentTransactionId, // Your internal payment ID
    // OR @NotBlank String gatewayTransactionId, // If webhook primarily uses this

    @NotNull PaymentTransactionStatus newStatus,
    String actualGatewayTransactionId, // If the main ID was an intent, this is the charge ID
    String paymentMethodDetails, // e.g., "Visa **** 4242"
    PaymentMethodType paymentMethodType,
    String gatewayErrorCode,
    String errorMessage,
    Map<String, Object> gatewayEventData // Raw event data from gateway for auditing
) {}