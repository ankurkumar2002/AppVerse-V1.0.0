package com.appverse.order_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(
    @NotBlank(message = "Application ID cannot be blank.")
    String applicationId,

    @NotNull(message = "Quantity cannot be null.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    int quantity,

    // Price will be fetched from app-service to ensure accuracy at time of order
    // ItemType could also be determined here or in the service logic

    @NotNull(message = "Item type cannot be null.") // Add this
    OrderItemTypeDto itemType // ONE_TIME_PURCHASE, SUBSCRIPTION_INITIAL_PURCHASE
) {}