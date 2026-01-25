package com.appverse.order_service.publisher;

import java.time.Instant;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.appverse.order_service.dto.PaymentUpdateDto;
import com.appverse.order_service.event.payload.OrderCancelledPayload;
import com.appverse.order_service.event.payload.OrderCreatedPayload;
import com.appverse.order_service.event.payload.OrderPaymentFailedPayload;
import com.appverse.order_service.event.payload.OrderPaymentSucceededPayload;
import com.appverse.order_service.model.CustomerOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_EVENTS_TOPIC = "order-events";

    /* ======================================================
       ORDER CREATED
       ====================================================== */
    public void publishOrderCreatedAfterCommit(CustomerOrder order) {

        OrderCreatedPayload payload = OrderCreatedPayload.from(order);

        publishAfterCommit(
                ORDER_EVENTS_TOPIC,
                order.getId(),
                payload,
                "OrderCreated"
        );
    }

    /* ======================================================
       PAYMENT EVENTS
       ====================================================== */
    public void publishPaymentEventAfterCommit(
            CustomerOrder order,
            PaymentUpdateDto dto,
            Instant timestamp) {

        Object payload = switch (dto.paymentStatus()) {
            case SUCCEEDED -> new OrderPaymentSucceededPayload(
                    order.getId(),
                    order.getUserId(),
                    order.getPaymentTransactionId(),
                    order.getOrderStatus(),
                    order.getOrderTotal(),
                    order.getCurrency(),
                    timestamp
            );
            case FAILED -> new OrderPaymentFailedPayload(
                    order.getId(),
                    order.getUserId(),
                    order.getPaymentTransactionId(),
                    order.getOrderStatus(),
                    dto.failureReason(),
                    timestamp
            );
            default -> null;
        };

        if (payload != null) {
            publishAfterCommit(
                    ORDER_EVENTS_TOPIC,
                    order.getId(),
                    payload,
                    "OrderPayment" + dto.paymentStatus()
            );
        }
    }

    /* ======================================================
       ORDER CANCELLED
       ====================================================== */
    public void publishOrderCancelledAfterCommit(CustomerOrder order) {

        OrderCancelledPayload payload = new OrderCancelledPayload(
                order.getId(),
                order.getUserId(),
                order.getOrderStatus(),
                "Cancelled by user",
                Instant.now()
        );

        publishAfterCommit(
                ORDER_EVENTS_TOPIC,
                order.getId(),
                payload,
                "OrderCancelled"
        );
    }

    /* ======================================================
       INTERNAL HELPER (TRANSACTION SAFE)
       ====================================================== */
    private void publishAfterCommit(
            String topic,
            String key,
            Object payload,
            String eventType) {

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        kafkaTemplate.send(topic, key, payload)
                                .whenComplete((result, ex) -> {
                                    if (ex == null) {
                                        log.info("{} event published for order {}", eventType, key);
                                    } else {
                                        log.error(
                                                "Failed to publish {} event for order {}",
                                                eventType,
                                                key,
                                                ex
                                        );
                                    }
                                });
                    }
                }
        );
    }
}
