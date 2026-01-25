// === In Order Service Project ===
package com.appverse.order_service.service.serviceImpl;

import com.appverse.order_service.aggregate.OrderAggregate;
import com.appverse.order_service.dto.*;
import com.appverse.order_service.enums.OrderStatus;
import com.appverse.order_service.exception.InvalidOrderStatusTransitionException;
import com.appverse.order_service.exception.ResourceNotFoundException;
import com.appverse.order_service.mapper.OrderMapper;
import com.appverse.order_service.model.CustomerOrder;
import com.appverse.order_service.publisher.OrderEventPublisher;
import com.appverse.order_service.repository.OrderRepository;
import com.appverse.order_service.service.OrderFactory;
import com.appverse.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderFactory orderFactory;
    private final OrderAggregate aggregate;
    private final OrderEventPublisher eventPublisher;

    private static final String ORDER_EVENTS_TOPIC = "order-events";

    /* ======================================================
       CREATE ORDER
       ====================================================== */
    @Override
    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {

        CustomerOrder order = orderFactory.create(userId, request);
        CustomerOrder savedOrder = orderRepository.save(order);

        eventPublisher.publishOrderCreatedAfterCommit(savedOrder);

        return orderMapper.toOrderResponse(savedOrder);
    }

    /* ======================================================
       PAYMENT UPDATE
       ====================================================== */
    @Override
    @Transactional
    public OrderResponse processPaymentUpdate(PaymentUpdateDto dto) {

        CustomerOrder order = orderRepository.findById(dto.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (isDuplicatePaymentUpdate(order, dto)) {
            log.warn("Duplicate payment update ignored for order {}", order.getId());
            return orderMapper.toOrderResponse(order);
        }

        order.setPaymentTransactionId(dto.paymentTransactionId());
        order.setPaymentStatus(dto.paymentStatus());

        Instant now = Instant.now();

        switch (dto.paymentStatus()) {
            case SUCCEEDED -> aggregate.applyPaymentSuccess(order, now);
            case FAILED -> aggregate.applyPaymentFailure(order);
            case PENDING -> order.setOrderStatus(OrderStatus.PAYMENT_PROCESSING);
        }

        CustomerOrder savedOrder = orderRepository.save(order);

        eventPublisher.publishPaymentEventAfterCommit(savedOrder, dto, now);

        return orderMapper.toOrderResponse(savedOrder);
    }

    private boolean isDuplicatePaymentUpdate(CustomerOrder order, PaymentUpdateDto dto) {
        if (order.getPaymentTransactionId() == null) {
            return false;
        }

        return order.getPaymentTransactionId().equals(dto.paymentTransactionId())
                && order.getPaymentStatus() == dto.paymentStatus()
                && order.getOrderStatus().isFinal();
    }

    /* ======================================================
       CANCEL ORDER
       ====================================================== */
    @Override
    @Transactional
    public OrderResponse cancelOrder(String orderId, String userId) {

        CustomerOrder order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + orderId + " for user " + userId));

        List<OrderStatus> cancellableStatuses = List.of(
                OrderStatus.PENDING_PAYMENT,
                OrderStatus.PAYMENT_PROCESSING
        );

        if (!cancellableStatuses.contains(order.getOrderStatus())) {
            throw new InvalidOrderStatusTransitionException(
                    "Order " + orderId + " cannot be cancelled in status " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED_BY_USER);

        CustomerOrder cancelledOrder = orderRepository.save(order);

        eventPublisher.publishOrderCancelledAfterCommit(cancelledOrder);

        return orderMapper.toOrderResponse(cancelledOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }
}
