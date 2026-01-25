package com.appverse.order_service.event.payload;

import com.appverse.order_service.enums.OrderStatus;
import com.appverse.order_service.model.CustomerOrder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderCreatedPayload(
    String orderId,
    String userId,
    OrderStatus orderStatus, // Will be PENDING_PAYMENT
    BigDecimal orderTotal,
    String currency,
    List<OrderItemSummaryPayload> items, // Summary of items
    Instant createdAt
) {

    public static OrderCreatedPayload from(CustomerOrder order) {
    return new OrderCreatedPayload(
            order.getId(),
            order.getUserId(),
            order.getOrderStatus(),
            order.getOrderTotal(),
            order.getCurrency(),
            order.getItems().stream()
                    .map(item -> new OrderItemSummaryPayload(
                            item.getId(),
                            item.getApplicationId(),
                            item.getApplicationName(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getTotalPrice(),
                            item.getItemType()
                    ))
                    .toList(),
            order.getCreatedAt()
    );
}

}