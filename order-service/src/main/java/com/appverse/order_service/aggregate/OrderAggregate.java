package com.appverse.order_service.aggregate;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import com.appverse.order_service.enums.FulfillmentStatus;
import com.appverse.order_service.enums.OrderStatus;
import com.appverse.order_service.exception.InvalidOrderStatusTransitionException;
import com.appverse.order_service.model.CustomerOrder;

@Component
public class OrderAggregate {

    public void validateCancellable(CustomerOrder order) {
        if (!List.of(
                OrderStatus.PENDING_PAYMENT,
                OrderStatus.PAYMENT_PROCESSING
        ).contains(order.getOrderStatus())) {
            throw new InvalidOrderStatusTransitionException(
                    "Order cannot be cancelled from state: " + order.getOrderStatus()
            );
        }
    }

    public void applyPaymentSuccess(CustomerOrder order, Instant now) {
        order.setOrderStatus(OrderStatus.AWAITING_FULFILLMENT);
        order.getItems().forEach(item ->
                item.setFulfillmentStatus(FulfillmentStatus.SUCCESSFUL)
        );
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(now);
    }

    public void applyPaymentFailure(CustomerOrder order) {
        order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
    }
}
