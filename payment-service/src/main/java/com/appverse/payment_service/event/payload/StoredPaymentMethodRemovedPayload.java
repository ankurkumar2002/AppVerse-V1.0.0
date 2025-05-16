// File: com/appverse/payment_service/event/payload/StoredPaymentMethodRemovedPayload.java
package com.appverse.payment_service.event.payload;

import com.appverse.payment_service.enums.PaymentGatewayType;
import java.time.Instant;

public record StoredPaymentMethodRemovedPayload(
    String storedPaymentMethodId,
    String userId,
    PaymentGatewayType paymentGateway, // For context
    String gatewayPaymentMethodId, // For context
    Instant removedAt
) {}