package com.appverse.order_service.dto;

import java.math.BigDecimal;

import com.appverse.order_service.enums.MonetizationType;

public record AppDetails(
            String id,
            String name,
            String version,
            BigDecimal price,
            String currency,
            MonetizationType monetizationType,
            boolean isFree
    ) {
    }
