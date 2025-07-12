
package com.appverse.app_service.event.payload;

import java.math.BigDecimal;
import java.time.Instant;

public record ApplicationPriceChangedPayload(
    String id,
    BigDecimal oldPrice,
    String oldCurrency,
    BigDecimal newPrice,
    String newCurrency,
    Instant priceChangedAt
) {}
