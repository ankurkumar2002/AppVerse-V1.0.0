// === In Order Service Project ===
package com.appverse.order_service.service.serviceImpl;

import com.appverse.order_service.client.AppServiceClient;
import com.appverse.order_service.dto.*;
import com.appverse.order_service.enums.FulfillmentStatus;
import com.appverse.order_service.enums.OrderItemType;
import com.appverse.order_service.enums.OrderStatus;
import com.appverse.order_service.enums.PaymentStatus;
import com.appverse.order_service.exception.InvalidOrderStatusTransitionException;
import com.appverse.order_service.exception.OrderProcessingException;
import com.appverse.order_service.exception.ResourceNotFoundException;
import com.appverse.order_service.mapper.OrderMapper;
import com.appverse.order_service.model.CustomerOrder;
import com.appverse.order_service.model.OrderItem;
import com.appverse.order_service.repository.OrderRepository;
import com.appverse.order_service.service.OrderService;
// import com.appverse.order_service.event.OrderEventPublisher; // Uncomment if using event publisher

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
// No explicit java.util.UUID import needed here if entities handle generation
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AppServiceClient appServiceClient;
    private final OrderMapper orderMapper;
    // private final OrderEventPublisher orderEventPublisher; // Uncomment if using event publisher

        @Override
    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        log.info("Attempting to create order for user ID: {}", userId);
        // ... (initial logs and checks for request & request.items()) ...
        log.info("Received CreateOrderRequest object: {}", request);
        if (request == null) { /* ... */ }
        if (request.items() == null) { /* ... */ }
        log.info("Number of items in request for user {}: {}", userId, request.items().size());

        // --- EXPLICITLY SET ID HERE ---
        String orderId = UUID.randomUUID().toString();
        log.info("Generated new Order ID: {}", orderId);

        CustomerOrder order = CustomerOrder.builder()
                .id(orderId) // <<<< ASSIGN THE ID HERE
                .userId(userId)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(null)
                .currency("USD")
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
                // ... (Feign call logic) ...
                log.debug("Fetching application details for ID: {}", itemRequest.applicationId());
                appDetails = appServiceClient.getAppDetails(itemRequest.applicationId());

                if (appDetails == null) { /* ... skip ... */ continue; }
                if (appDetails.isFree() && itemRequest.itemType() == OrderItemTypeDto.ONE_TIME_PURCHASE) { /* ... skip ... */ continue; }

            } catch (FeignException e) { /* ... handle ... */ throw new OrderProcessingException("Problem Occured During feign call",e); }

            OrderItemType itemTypeModel = convertOrderItemTypeDtoToModel(itemRequest.itemType());
            BigDecimal itemUnitPrice = appDetails.price() != null ? appDetails.price() : BigDecimal.ZERO;
            BigDecimal itemTotalPrice = itemUnitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

            if (orderCurrency == null && appDetails.currency() != null && !appDetails.currency().isBlank()) { /* ... */ }
            else if (appDetails.currency() != null && !appDetails.currency().isBlank() && (orderCurrency == null || !orderCurrency.equals(appDetails.currency())) ) { /* ... */ }


            // --- EXPLICITLY SET OrderItem ID HERE ---
            String orderItemId = UUID.randomUUID().toString();
            log.debug("Generated new OrderItem ID: {} for App ID: {}", orderItemId, itemRequest.applicationId());

            OrderItem orderItem = OrderItem.builder()
                    .id(orderItemId) // <<<< ASSIGN THE ID HERE
                    .applicationId(appDetails.id())
                    .applicationName(appDetails.name())
                    .applicationVersion(appDetails.version())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemUnitPrice)
                    .totalPrice(itemTotalPrice)
                    .currency(appDetails.currency())
                    .itemType(itemTypeModel)
                    .fulfillmentStatus(FulfillmentStatus.PENDING)
                    .build();
            order.addItem(orderItem); // This will also set orderItem.setCustomerOrder(order)
            calculatedOrderTotal = calculatedOrderTotal.add(itemTotalPrice);
            log.debug("END processing item. App ID: {}. OrderItem ID: {}", itemRequest.applicationId(), orderItem.getId());
        }

        // ... (rest of the method: check if order.getItems().isEmpty(), set totals, save) ...
        if (order.getItems().isEmpty()) { /* ... */ }
        order.setOrderTotal(calculatedOrderTotal);
        order.setCurrency(orderCurrency != null ? orderCurrency : "USD");

        CustomerOrder savedOrder = orderRepository.save(order); // Now 'order' and its 'items' definitely have IDs
        log.info("Order {} created successfully for user ID: {}. Total: {} {}",
                savedOrder.getId(), userId, savedOrder.getOrderTotal(), savedOrder.getCurrency());

        return orderMapper.toOrderResponse(savedOrder);
    }

    // ... rest of OrderServiceImpl ...

    // ... rest of your OrderServiceImpl ...

    private OrderItemType convertOrderItemTypeDtoToModel(OrderItemTypeDto dtoType) {
        if (dtoType == null) {
            log.error("OrderItemTypeDto is null, cannot convert to model type.");
            throw new IllegalArgumentException("OrderItemTypeDto cannot be null.");
        }
        log.debug("Converting OrderItemTypeDto: {}", dtoType);
        return switch (dtoType) {
            case ONE_TIME_PURCHASE -> OrderItemType.ONE_TIME_PURCHASE;
            case SUBSCRIPTION_INITIAL_PURCHASE -> OrderItemType.SUBSCRIPTION_INITIAL_PURCHASE;
            // Add more cases if your DTO enum expands
            // default -> throw new IllegalArgumentException("Unknown OrderItemTypeDto: " + dtoType); // This would be better if enum had more values
        };
    }

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

    @Override
    @Transactional
    public OrderResponse processPaymentUpdate(PaymentUpdateDto paymentUpdateDto) {
        log.info("Processing payment update for order ID: {}, transaction ID: {}, status: {}",
                paymentUpdateDto.orderId(), paymentUpdateDto.paymentTransactionId(), paymentUpdateDto.paymentStatus());

        CustomerOrder order = orderRepository.findById(paymentUpdateDto.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + paymentUpdateDto.orderId()));

        // Idempotency: Check if this specific update has already been processed or if order is in a final state
        if (List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED_BY_USER, OrderStatus.CANCELLED_BY_SYSTEM, OrderStatus.REFUNDED).contains(order.getOrderStatus())) {
            log.warn("Order {} is already in a final state ({}). Ignoring payment update.", order.getId(), order.getOrderStatus());
            return orderMapper.toOrderResponse(order);
        }
        if (order.getPaymentTransactionId() != null &&
            order.getPaymentTransactionId().equals(paymentUpdateDto.paymentTransactionId()) &&
            order.getPaymentStatus() == paymentUpdateDto.paymentStatus()) {
            log.warn("Payment update for order {} with transaction {} and status {} appears to be a duplicate. Ignoring.",
                    order.getId(), paymentUpdateDto.paymentTransactionId(), paymentUpdateDto.paymentStatus());
            return orderMapper.toOrderResponse(order);
        }


        order.setPaymentTransactionId(paymentUpdateDto.paymentTransactionId());
        order.setPaymentStatus(paymentUpdateDto.paymentStatus()); // Store denormalized payment status

        if (paymentUpdateDto.paymentStatus() == PaymentStatus.SUCCEEDED) {
            order.setOrderStatus(OrderStatus.AWAITING_FULFILLMENT); // Or PROCESSING
            log.info("Order {} payment SUCCEEDED. Status set to AWAITING_FULFILLMENT. Transaction ID: {}",
                    order.getId(), paymentUpdateDto.paymentTransactionId());

            // TODO: Trigger fulfillment process (e.g., publish OrderPaidEvent, call fulfillment service)
            // orderEventPublisher.publishOrderPaidEvent(orderMapper.toOrderResponse(order));

            // For simplicity, let's assume immediate fulfillment for digital goods.
            // In a real system, this would be more complex and potentially asynchronous.
            boolean allFulfilled = true;
            for (OrderItem item : order.getItems()) {
                // Implement actual fulfillment logic here or via event listener
                // For now, just marking as successful
                item.setFulfillmentStatus(FulfillmentStatus.SUCCESSFUL);
                log.info("Item {} in order {} marked as FULFILLED.", item.getId(), order.getId());
            }
            if (allFulfilled) {
                order.setOrderStatus(OrderStatus.COMPLETED);
                order.setCompletedAt(Instant.now());
                log.info("Order {} fully fulfilled and COMPLETED.", order.getId());
            }

        } else if (paymentUpdateDto.paymentStatus() == PaymentStatus.FAILED) {
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            log.warn("Order {} payment FAILED. Transaction ID: {}. Reason: {}",
                    order.getId(), paymentUpdateDto.paymentTransactionId(), paymentUpdateDto.failureReason());
        } else if (paymentUpdateDto.paymentStatus() == PaymentStatus.PENDING) {
            order.setOrderStatus(OrderStatus.PAYMENT_PROCESSING); // Or keep as PENDING_PAYMENT if that's your flow
            log.info("Order {} payment is PENDING. Status set to PAYMENT_PROCESSING. Transaction ID: {}",
                    order.getId(), paymentUpdateDto.paymentTransactionId());
        } else {
            // Handle other statuses if necessary (e.g., REFUNDED might come through here or a separate flow)
            log.info("Order {} received payment status: {}. Current order status: {}",
                    order.getId(), paymentUpdateDto.paymentStatus(), order.getOrderStatus());
        }

        CustomerOrder updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String orderId, String userId) {
        log.info("User {} attempting to cancel order ID: {}", userId, orderId);
        CustomerOrder order = orderRepository.findByIdAndUserId(orderId, userId) // Ensure user owns the order
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId + " for user " + userId));

        // Define cancellable statuses
        List<OrderStatus> cancellableStatuses = List.of(
                OrderStatus.PENDING_PAYMENT,
                OrderStatus.PAYMENT_PROCESSING // Depending on policy, might allow cancellation if payment not confirmed
        );

        if (!cancellableStatuses.contains(order.getOrderStatus())) {
            log.warn("Order {} cannot be cancelled by user. Current status: {}", orderId, order.getOrderStatus());
            throw new InvalidOrderStatusTransitionException(
                    "Order " + orderId + " cannot be cancelled. Current status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED_BY_USER);
        // TODO: If a payment was initiated (e.g., with Payment Service and has a gateway transaction ID in PENDING state),
        // an attempt should be made to void/cancel that payment with the payment gateway.
        // This requires coordination with the Payment Service.
        // if (order.getPaymentTransactionId() != null && order.getPaymentStatus() == PaymentStatus.PENDING) {
        //     // Call paymentService.cancelPayment(order.getPaymentTransactionId());
        // }

        CustomerOrder cancelledOrder = orderRepository.save(order);
        log.info("Order {} cancelled successfully by user {}.", orderId, userId);
        return orderMapper.toOrderResponse(cancelledOrder);
    }
}