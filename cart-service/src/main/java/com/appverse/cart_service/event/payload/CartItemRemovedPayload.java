// === In cart-service Project ===
package com.appverse.cart_service.event.payload;

import java.time.Instant;

public record CartItemRemovedPayload(
    String cartId,
    String userId,
    String cartItemId,
    String applicationId,
    String applicationName, // For context
    int quantityRemoved,   // The quantity that was in the item before removal
    Instant eventTimestamp
) {}