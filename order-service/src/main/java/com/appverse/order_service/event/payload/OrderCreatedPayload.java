package com.appverse.order_service.event.payload;

import com.appverse.order_service.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderCreatedPayload(
    String orderId,
    String userId,
    OrderStatus orderStatus, // Will be PENDING_PAYMENT
    BigDecimal orderTotal,
    String currency,
    List<OrderItemSummaryPayload> items, // Summary of items
    Instant createdAt
) {}