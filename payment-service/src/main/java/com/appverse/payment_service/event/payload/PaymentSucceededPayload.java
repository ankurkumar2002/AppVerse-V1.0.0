// File: com/appverse/payment_service/event/payload/PaymentSucceededPayload.java
package com.appverse.payment_service.event.payload;

import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentMethodType; // Assuming you have this enum
import com.appverse.payment_service.enums.PaymentReferenceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record PaymentSucceededPayload(
    String paymentTransactionId,
    String userId,
    String referenceId,
    PaymentReferenceType referenceType,
    BigDecimal amount,
    String currency,
    PaymentGatewayType paymentGateway,
    String gatewayTransactionId,
    String gatewayPaymentIntentId, // If applicable
    PaymentMethodType paymentMethodType, // e.g., CARD, BANK_TRANSFER
    String paymentMethodDetails, // e.g., "Visa **** 4242"
    Map<String, Object> metadata,
    Instant processedAt
) {}