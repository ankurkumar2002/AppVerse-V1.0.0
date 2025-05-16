// File: com/appverse/payment_service/event/payload/StoredPaymentMethodDefaultChangedPayload.java
package com.appverse.payment_service.event.payload;

import com.appverse.payment_service.enums.PaymentGatewayType;
import java.time.Instant;

public record StoredPaymentMethodDefaultChangedPayload(
    String newDefaultStoredPaymentMethodId,
    String oldDefaultStoredPaymentMethodId, // Can be null if no previous default
    String userId,
    PaymentGatewayType paymentGateway,
    Instant changedAt
) {}