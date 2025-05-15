package com.appverse.cart_service.dto;
import jakarta.validation.constraints.Min;
public record UpdateCartItemQuantityRequest(
    @Min(0) int newQuantity // 0 means remove
) {}