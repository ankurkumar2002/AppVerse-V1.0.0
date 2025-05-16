package com.appverse.order_service.event.payload;

import com.appverse.order_service.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderPaymentSucceededPayload(
    String orderId,
    String userId,
    String paymentTransactionId, // From PaymentUpdateDto
    OrderStatus newOrderStatus,    // Will be AWAITING_FULFILLMENT or COMPLETED
    BigDecimal orderTotal,
    String currency,
    Instant paymentProcessedAt // Or just use event timestamp
) {}