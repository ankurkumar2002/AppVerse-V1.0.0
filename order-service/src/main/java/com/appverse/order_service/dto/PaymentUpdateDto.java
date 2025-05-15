package com.appverse.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.appverse.order_service.enums.PaymentStatus;


public record PaymentUpdateDto(
    @NotNull String orderId,
    @NotBlank String paymentTransactionId, // The ID from the payment service
    @NotNull PaymentStatus paymentStatus, // e.g., SUCCEEDED, FAILED
    String failureReason // Optional
) {}