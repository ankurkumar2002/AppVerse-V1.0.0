// File: com/appverse/payment_service/event/payload/StoredPaymentMethodAddedPayload.java
package com.appverse.payment_service.event.payload;

import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentMethodType;
import com.appverse.payment_service.enums.StoredPaymentMethodStatus;

import java.time.Instant;

public record StoredPaymentMethodAddedPayload(
    String storedPaymentMethodId,
    String userId,
    PaymentGatewayType paymentGateway,
    String gatewayCustomerId,
    String gatewayPaymentMethodId,
    PaymentMethodType type,
    String brand,
    String last4,
    Integer expiryMonth,
    Integer expiryYear,
    boolean isDefault,
    StoredPaymentMethodStatus status
    // Instant createdAt
) {}