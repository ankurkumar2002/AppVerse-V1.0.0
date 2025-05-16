package com.appverse.order_service.event.payload;

import com.appverse.order_service.enums.OrderStatus;
import java.time.Instant;

public record OrderPaymentFailedPayload(
    String orderId,
    String userId,
    String paymentTransactionId, // From PaymentUpdateDto
    OrderStatus newOrderStatus,    // Will be PAYMENT_FAILED
    String failureReason,      // From PaymentUpdateDto
    Instant paymentFailedAt
) {}