package com.appverse.payment_service.dto;

import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentReferenceType;
import com.appverse.payment_service.enums.PaymentTransactionStatus;
import com.appverse.payment_service.enums.PaymentMethodType;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record PaymentResponse(
    String id,
    String userId,
    String payerEmail,
    String referenceId,
    PaymentReferenceType referenceType,
    BigDecimal amount,
    String currency,
    PaymentGatewayType paymentGateway,
    String gatewayTransactionId,
    String gatewayPaymentIntentId,
    PaymentMethodType paymentMethodType,
    String paymentMethodDetails,
    PaymentTransactionStatus status,
    String description,
    String errorMessage,
    String gatewayErrorCode,
    Map<String, Object> metadata, // Assuming metadata is stored as JSON and parsed
    Instant initiatedAt,
    Instant processedAt,
    Instant updatedAt
) {}