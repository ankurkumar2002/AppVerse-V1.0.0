package com.appverse.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
    // userId will be extracted from JWT in the controller
    @NotEmpty(message = "Order must contain at least one item.")
    List<@Valid CreateOrderItemRequest> items
    // Potentially other details like shippingAddressId, billingAddressId if applicable later
) {}