// === In cart-service Project ===
package com.appverse.cart_service.event.payload;

import java.time.Instant;
import java.util.List;

public record CartClearedPayload(
    String cartId,
    String userId,
    int numberOfItemsCleared,
    List<String> clearedApplicationIds, // IDs of applications that were in the cart
    Instant eventTimestamp
) {}