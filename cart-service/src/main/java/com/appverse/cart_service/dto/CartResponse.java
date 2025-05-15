package com.appverse.cart_service.dto;


import java.time.Instant;
import java.util.List;
import java.util.UUID;



public record CartResponse(
    UUID cartId, // Or String if your Cart.id is String
    String userId,
    List<CartItemResponse> items,
    Instant createdAt,
    Instant updatedAt
    // BigDecimal totalPrice, // You'd calculate this in the service or mapper
    // String currency // Overall cart currency
) {}