package com.appverse.order_service.dto;

import com.appverse.order_service.enums.FulfillmentStatus;
import com.appverse.order_service.enums.OrderItemType; // Model enum for response

import java.math.BigDecimal;
import java.time.Instant;


public record OrderItemResponse(
    String id,
    String applicationId,
    String applicationName,
    String applicationVersion,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice,
    String currency,
    OrderItemType itemType, // Using the model enum here
    String subscriptionPlanId, // Nullable
    FulfillmentStatus fulfillmentStatus,
    Instant createdAt
) {}