package com.appverse.cart_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
public record AddItemToCartRequest(
    @NotBlank String applicationId,
    @Min(1) int quantity
) {}