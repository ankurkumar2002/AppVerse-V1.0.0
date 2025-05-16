// File: com/appverse/payment_service/event/payload/PaymentFailedPayload.java
package com.appverse.payment_service.event.payload;

import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentReferenceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record PaymentFailedPayload(
    String paymentTransactionId,
    String userId,
    String referenceId,
    PaymentReferenceType referenceType,
    BigDecimal amount, // Amount attempted
    String currency,
    PaymentGatewayType paymentGateway,
    String gatewayPaymentIntentId, // If applicable
    String errorMessage,
    String gatewayErrorCode,
    Map<String, Object> metadata,
    Instant failedAt
) {}