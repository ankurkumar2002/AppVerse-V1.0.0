// === In cart-service Project ===
package com.appverse.cart_service.event.payload;

import java.math.BigDecimal;
import java.time.Instant;

public record CartItemAddedPayload(
    String cartId,
    String userId,
    String cartItemId, // ID of the CartItem entity
    String applicationId,
    String applicationName,
    int quantityAdded,   // The quantity that was added in this operation
    int newTotalQuantity, // The new total quantity of this item in the cart
    BigDecimal unitPrice,
    String currency,
    Instant eventTimestamp
) {}