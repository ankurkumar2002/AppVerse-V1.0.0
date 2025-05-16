package com.appverse.order_service.event.payload;

import com.appverse.order_service.enums.OrderStatus;
import java.time.Instant;

public record OrderCancelledPayload(
    String orderId,
    String userId,
    OrderStatus newOrderStatus, // CANCELLED_BY_USER or CANCELLED_BY_SYSTEM
    String reason,
    Instant cancelledAt
) {}