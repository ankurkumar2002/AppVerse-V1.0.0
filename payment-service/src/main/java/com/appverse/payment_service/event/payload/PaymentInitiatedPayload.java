// === In Payment Service Project ===
// New package: com.appverse.payment_service.event.payload

// File: com/appverse/payment_service/event/payload/PaymentInitiatedPayload.java
package com.appverse.payment_service.event.payload;

import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentReferenceType;
import com.appverse.payment_service.enums.PaymentTransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record PaymentInitiatedPayload(
    String paymentTransactionId,
    String userId,
    String referenceId,
    PaymentReferenceType referenceType,
    BigDecimal amount,
    String currency,
    PaymentGatewayType paymentGateway,
    PaymentTransactionStatus initialStatus, // e.g., PENDING_CREATION, PENDING_GATEWAY_ACTION
    String description,
    Map<String, Object> metadata,
    Instant initiatedAt
) {}