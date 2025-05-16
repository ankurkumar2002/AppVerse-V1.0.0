// File: com/appverse/payment_service/event/payload/PaymentRequiresActionPayload.java
package com.appverse.payment_service.event.payload;

import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentReferenceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record PaymentRequiresActionPayload(
    String paymentTransactionId,
    String userId,
    String referenceId,
    PaymentReferenceType referenceType,
    BigDecimal amount,
    String currency,
    PaymentGatewayType paymentGateway,
    String gatewayPaymentIntentId,
    String clientSecret, // For frontend to handle actions like 3DS
    Map<String, Object> metadata,
    Instant eventTimestamp
) {}