package com.appverse.cart_service.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CartItemResponse(
    String applicationId,
    String applicationName,
    int quantity,
    BigDecimal unitPrice,
    String currency,
    boolean isFree,
    String thumbnailUrl,
    Instant addedAt
) {}