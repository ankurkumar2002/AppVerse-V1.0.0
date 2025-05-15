package com.appverse.order_service.dto;

import com.appverse.order_service.enums.OrderStatus;
import com.appverse.order_service.enums.PaymentStatus; // Assuming you have this enum

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
    String id,
    String userId,
    OrderStatus orderStatus,
    BigDecimal orderTotal,
    String currency,
    String paymentTransactionId, // Nullable
    PaymentStatus paymentStatus, // Nullable
    List<OrderItemResponse> items,
    Instant createdAt,
    Instant updatedAt,
    Instant completedAt // Nullable
) {}
