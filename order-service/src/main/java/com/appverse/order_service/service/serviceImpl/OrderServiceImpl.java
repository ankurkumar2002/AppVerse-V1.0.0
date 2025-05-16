// === In Order Service Project ===
package com.appverse.order_service.service.serviceImpl;

import com.appverse.order_service.client.AppServiceClient;
import com.appverse.order_service.dto.*;
import com.appverse.order_service.enums.FulfillmentStatus;
import com.appverse.order_service.enums.MonetizationType;
import com.appverse.order_service.enums.OrderItemType;
import com.appverse.order_service.enums.OrderStatus;
import com.appverse.order_service.enums.PaymentStatus; // Your existing PaymentStatus enum
import com.appverse.order_service.event.payload.*; // <<< IMPORT EVENT PAYLOADS
import com.appverse.order_service.exception.InvalidOrderStatusTransitionException;
import com.appverse.order_service.exception.OrderProcessingException;
import com.appverse.order_service.exception.ResourceNotFoundException;
import com.appverse.order_service.exception.ServiceUnavailableException; // Assuming you have this
import com.appverse.order_service.mapper.OrderMapper;
import com.appverse.order_service.model.CustomerOrder;
import com.appverse.order_service.model.OrderItem;
import com.appverse.order_service.repository.OrderRepository;
import com.appverse.order_service.service.OrderService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate; // <<< IMPORT KAFKA
import org.springframework.kafka.support.SendResult;  // <<< IMPORT KAFKA
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture; // <<< IMPORT KAFKA
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AppServiceClient appServiceClient;
    private final OrderMapper orderMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate; // <<< INJECT KAFKA

    private static final String ORDER_EVENTS_TOPIC = "order-events"; // Define topic

    @Override
    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        log.info("Attempting to create order for user ID: {}", userId);
        log.info("Received CreateOrderRequest object: {}", request);
        if (request == null) { throw new OrderProcessingException("Invalid order request: request object is null."); }
        if (request.items() == null) { throw new OrderProcessingException("Order must contain at least one item (items list is null)."); }
        log.info("Number of items in request for user {}: {}", userId, request.items().size());

        String orderId = UUID.randomUUID().toString();
        log.info("Generated new Order ID: {}", orderId);

        CustomerOrder order = CustomerOrder.builder()
                .id(orderId)
                .userId(userId)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(null) // Assuming PaymentStatus is your enum from order-service
                .currency("USD") // Will be overridden by item currency if consistent
                .build();
        log.info("CustomerOrder object built successfully with ID: {}", order.getId());

        BigDecimal calculatedOrderTotal = BigDecimal.ZERO;
        String orderCurrency = null;

        if (request.items().isEmpty()) {
            log.warn("CreateOrderRequest.items() is empty for user ID: {}", userId);
            throw new OrderProcessingException("Order must contain at least one item (items list is empty).");
        }
        log.info("About to start iterating through items.");

        for (CreateOrderItemRequest itemRequest : request.items()) {
            log.debug("START processing item. App ID: {}", itemRequest.applicationId());
            AppServiceClient.AppDetails appDetails;
            try {
                log.debug("Fetching application details for ID: {}", itemRequest.applicationId());
                appDetails = appServiceClient.getAppDetails(itemRequest.applicationId());

                if (appDetails == null) {
                    log.warn("Application details not found for ID: {}. Skipping item.", itemRequest.applicationId());
                    continue;
                }
                // Logic for handling app's monetizationType vs. itemRequest.itemType()
                if (appDetails.monetizationType() == MonetizationType.SUBSCRIPTION_ONLY && // <<< Use the imported enum
                    itemRequest.itemType() == OrderItemTypeDto.ONE_TIME_PURCHASE) {
                    log.warn("Attempt to ONE_TIME_PURCHASE a SUBSCRIPTION_ONLY app {}. Skipping.", appDetails.id());
                    continue;
                }
                if (appDetails.monetizationType() == MonetizationType.ONE_TIME_PURCHASE && // <<< Use the imported enum
                    itemRequest.itemType() == OrderItemTypeDto.SUBSCRIPTION_INITIAL_PURCHASE) {
                    log.warn("Attempt to SUBSCRIBE to a ONE_TIME_PURCHASE app {}. Skipping.", appDetails.id());
                    continue;
                }
                 if (appDetails.isFree() && itemRequest.itemType() == OrderItemTypeDto.ONE_TIME_PURCHASE) {
                    log.info("Skipping free application {} requested as ONE_TIME_PURCHASE, but will add as 0 price.", itemRequest.applicationId());
                    // If you want to add free items to the order, ensure price is 0.
                    // If you want to skip them entirely, use 'continue;' here after logging.
                    // For now, let's assume we add it with price 0 if it's a ONE_TIME_PURCHASE request for a free app.
                }


            } catch (FeignException e) {
                log.error("FeignException fetching app details for ID {}: Status {}, Message: {}",
                        itemRequest.applicationId(), e.status(), e.getMessage(), e);
                throw new ServiceUnavailableException("Could not retrieve application details for ID: " + itemRequest.applicationId()+ e);
            }

            OrderItemType itemTypeModel = convertOrderItemTypeDtoToModel(itemRequest.itemType());
            BigDecimal itemUnitPrice = (appDetails.isFree() && itemTypeModel == OrderItemType.ONE_TIME_PURCHASE) ? BigDecimal.ZERO :
                                       (appDetails.price() != null ? appDetails.price() : BigDecimal.ZERO);
            BigDecimal itemTotalPrice = itemUnitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

            String itemCurrency = (itemUnitPrice.compareTo(BigDecimal.ZERO) == 0) ? "USD" : appDetails.currency(); // Default currency if free

            if (orderCurrency == null && itemCurrency != null && !itemCurrency.isBlank()) {
                orderCurrency = itemCurrency;
            } else if (itemCurrency != null && !itemCurrency.isBlank() && !orderCurrency.equals(itemCurrency)) {
                log.error("Order creation failed: Items with different currencies. Order currency: {}, Item currency: {}", orderCurrency, itemCurrency);
                throw new OrderProcessingException("Items with different currencies are not supported in a single order.");
            }

            String orderItemId = UUID.randomUUID().toString();
            OrderItem orderItem = OrderItem.builder()
                    .id(orderItemId)
                    .applicationId(appDetails.id())
                    .applicationName(appDetails.name())
                    .applicationVersion(appDetails.version())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemUnitPrice)
                    .totalPrice(itemTotalPrice)
                    .currency(itemCurrency)
                    .itemType(itemTypeModel)
                    .fulfillmentStatus(FulfillmentStatus.PENDING)
                    .build();
            order.addItem(orderItem);
            calculatedOrderTotal = calculatedOrderTotal.add(itemTotalPrice);
            log.debug("END processing item. App ID: {}. OrderItem ID: {}", itemRequest.applicationId(), orderItem.getId());
        }

        if (order.getItems().isEmpty()) {
            log.warn("Order for user {} resulted in no valid items after processing.", userId);
            throw new OrderProcessingException("No valid items found to create an order.");
        }

        order.setOrderTotal(calculatedOrderTotal);
        order.setCurrency(orderCurrency != null ? orderCurrency : "USD");

        CustomerOrder savedOrder = orderRepository.save(order);
        log.info("Order {} created successfully for user ID: {}. Total: {} {}",
                savedOrder.getId(), userId, savedOrder.getOrderTotal(), savedOrder.getCurrency());

        // --- Publish OrderCreatedEvent ---
        List<OrderItemSummaryPayload> itemSummaries = savedOrder.getItems().stream()
                .map(oi -> new OrderItemSummaryPayload(
                        oi.getId(), oi.getApplicationId(), oi.getApplicationName(),
                        oi.getQuantity(), oi.getUnitPrice(), oi.getTotalPrice(), oi.getItemType()))
                .collect(Collectors.toList());

        OrderCreatedPayload createdPayload = new OrderCreatedPayload(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getOrderStatus(),
                savedOrder.getOrderTotal(),
                savedOrder.getCurrency(),
                itemSummaries,
                savedOrder.getCreatedAt() // Assuming JPA Auditing populates this
        );
        publishKafkaEvent(ORDER_EVENTS_TOPIC, savedOrder.getId(), "OrderCreated", createdPayload);

        return orderMapper.toOrderResponse(savedOrder);
    }


    @Override
    @Transactional
    public OrderResponse processPaymentUpdate(PaymentUpdateDto paymentUpdateDto) {
        log.info("Processing payment update for order ID: {}, transaction ID: {}, status from DTO: {}",
                paymentUpdateDto.orderId(), paymentUpdateDto.paymentTransactionId(), paymentUpdateDto.paymentStatus());

        CustomerOrder order = orderRepository.findById(paymentUpdateDto.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + paymentUpdateDto.orderId()));

        // Idempotency checks
        if (List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED_BY_USER, OrderStatus.CANCELLED_BY_SYSTEM, OrderStatus.REFUNDED).contains(order.getOrderStatus())) {
            log.warn("Order {} already in a final state ({}). Ignoring payment update.", order.getId(), order.getOrderStatus());
            return orderMapper.toOrderResponse(order);
        }
        // Assuming PaymentUpdateDto.paymentStatus() is now your PaymentStatus enum
        if (order.getPaymentTransactionId() != null &&
            order.getPaymentTransactionId().equals(paymentUpdateDto.paymentTransactionId()) &&
            order.getPaymentStatus() == paymentUpdateDto.paymentStatus()) { // Direct enum comparison
            log.warn("Payment update for order {} with transaction {} and status {} appears to be a duplicate. Ignoring.",
                    order.getId(), paymentUpdateDto.paymentTransactionId(), paymentUpdateDto.paymentStatus());
            return orderMapper.toOrderResponse(order);
        }

        order.setPaymentTransactionId(paymentUpdateDto.paymentTransactionId());
        order.setPaymentStatus(paymentUpdateDto.paymentStatus()); // Assign the enum directly

        Instant eventTimestamp = Instant.now();

        if (paymentUpdateDto.paymentStatus() == PaymentStatus.SUCCEEDED) {
            order.setOrderStatus(OrderStatus.AWAITING_FULFILLMENT);
            log.info("Order {} payment SUCCEEDED. Status set to AWAITING_FULFILLMENT. Transaction ID: {}",
                    order.getId(), paymentUpdateDto.paymentTransactionId());

            // TODO: Trigger fulfillment process (e.g., publish OrderPaidEvent, call fulfillment service)
            // This is where you'd grant access to one-time purchase items or notify subscription service
            // For now, mock fulfillment:
            boolean allFulfilled = true;
            for (OrderItem item : order.getItems()) {
                item.setFulfillmentStatus(FulfillmentStatus.SUCCESSFUL); // Mark as fulfilled
                log.info("Item {} in order {} marked as FULFILLED (mock).", item.getId(), order.getId());
            }
            if (allFulfilled) {
                order.setOrderStatus(OrderStatus.COMPLETED);
                order.setCompletedAt(eventTimestamp);
                log.info("Order {} fully fulfilled (mock) and COMPLETED.", order.getId());
            }
            // Publish OrderPaymentSucceededEvent
            OrderPaymentSucceededPayload succeededPayload = new OrderPaymentSucceededPayload(
                    order.getId(), order.getUserId(), order.getPaymentTransactionId(),
                    order.getOrderStatus(), order.getOrderTotal(), order.getCurrency(), eventTimestamp);
            publishKafkaEvent(ORDER_EVENTS_TOPIC, order.getId(), "OrderPaymentSucceeded", succeededPayload);

        } else if (paymentUpdateDto.paymentStatus() == PaymentStatus.FAILED) {
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            log.warn("Order {} payment FAILED. Transaction ID: {}. Reason: {}",
                    order.getId(), paymentUpdateDto.paymentTransactionId(), paymentUpdateDto.failureReason());
            // Publish OrderPaymentFailedEvent
            OrderPaymentFailedPayload failedPayload = new OrderPaymentFailedPayload(
                    order.getId(), order.getUserId(), order.getPaymentTransactionId(),
                    order.getOrderStatus(), paymentUpdateDto.failureReason(), eventTimestamp);
            publishKafkaEvent(ORDER_EVENTS_TOPIC, order.getId(), "OrderPaymentFailed", failedPayload);

        } else if (paymentUpdateDto.paymentStatus() == PaymentStatus.PENDING) {
            order.setOrderStatus(OrderStatus.PAYMENT_PROCESSING);
            log.info("Order {} payment is PENDING. Status set to PAYMENT_PROCESSING. Transaction ID: {}",
                    order.getId(), paymentUpdateDto.paymentTransactionId());
            // Optionally publish an OrderPaymentPendingEvent
        } else {
            log.info("Order {} received unhandled payment status: {}. Current order status: {}",
                    order.getId(), paymentUpdateDto.paymentStatus(), order.getOrderStatus());
        }

        CustomerOrder updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }


    @Override
    @Transactional
    public OrderResponse cancelOrder(String orderId, String userId) {
        log.info("User {} attempting to cancel order ID: {}", userId, orderId);
        CustomerOrder order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId + " for user " + userId));

        List<OrderStatus> cancellableStatuses = List.of(
                OrderStatus.PENDING_PAYMENT,
                OrderStatus.PAYMENT_PROCESSING
        );

        if (!cancellableStatuses.contains(order.getOrderStatus())) {
            log.warn("Order {} cannot be cancelled by user. Current status: {}", orderId, order.getOrderStatus());
            throw new InvalidOrderStatusTransitionException(
                    "Order " + orderId + " cannot be cancelled. Current status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED_BY_USER);
        // TODO: If a payment was initiated, attempt to void/cancel it with payment gateway.
        // This requires coordination with the Payment Service.

        CustomerOrder cancelledOrder = orderRepository.save(order);
        log.info("Order {} cancelled successfully by user {}.", orderId, userId);

        // Publish OrderCancelledEvent
        OrderCancelledPayload cancelledPayload = new OrderCancelledPayload(
                cancelledOrder.getId(), cancelledOrder.getUserId(), cancelledOrder.getOrderStatus(),
                "Cancelled by user", Instant.now()
        );
        publishKafkaEvent(ORDER_EVENTS_TOPIC, cancelledOrder.getId(), "OrderCancelled", cancelledPayload);

        return orderMapper.toOrderResponse(cancelledOrder);
    }


    // Helper method to publish Kafka events with logging
    private void publishKafkaEvent(String topic, String key, String eventType, Object payload) {
        log.debug("Preparing to publish {} event with key {} to topic {}", eventType, key, topic);
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent {} event to topic {} for key {}: offset {}, partition {}",
                        eventType, topic, key, result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
            } else {
                log.error("Failed to send {} event to topic {} for key {}: {}",
                        eventType, topic, key, ex.getMessage(), ex);
                // Consider further error handling here if send failure is critical
            }
        });
        log.debug("Asynchronously published {} event for key {}. Callback will log success/failure.", eventType, key);
    }

    // Helper to convert DTO enum to Model enum (as provided before)
    private OrderItemType convertOrderItemTypeDtoToModel(OrderItemTypeDto dtoType) {
        if (dtoType == null) { /* ... throw error ... */ }
        return switch (dtoType) {
            case ONE_TIME_PURCHASE -> OrderItemType.ONE_TIME_PURCHASE;
            case SUBSCRIPTION_INITIAL_PURCHASE -> OrderItemType.SUBSCRIPTION_INITIAL_PURCHASE;
        };
    }

    // getOrderById and getAllUserOrders remain the same (no event publishing)
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId) {
        log.debug("Fetching order by ID: {}", orderId);
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(String userId) {
        log.debug("Fetching orders for user ID: {}", userId);
        List<CustomerOrder> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }
}