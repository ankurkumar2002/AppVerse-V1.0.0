// === In cart-service Project ===
package com.appverse.cart_service.event.payload;

import java.time.Instant;

public record CartItemQuantityUpdatedPayload(
    String cartId,
    String userId,
    String cartItemId,
    String applicationId,
    int oldQuantity,
    int newQuantity,
    Instant eventTimestamp
) {}