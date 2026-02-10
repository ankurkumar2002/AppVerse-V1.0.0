
// === In Order Service Project ===
package com.appverse.order_service.controller;

import com.appverse.order_service.dto.CreateOrderRequest;
import com.appverse.order_service.dto.OrderResponse;
import com.appverse.order_service.dto.PaymentUpdateDto;
import com.appverse.order_service.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
// No UUID import needed if IDs are String

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject(); // 'sub' claim usually holds the Keycloak User ID
        log.info("Received request to create order for user ID: {}", userId);

        OrderResponse createdOrder = orderService.createOrder(userId, createOrderRequest);

        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdOrder.id())
                .toUri();

        log.info("Order {} created successfully for user {}", createdOrder.id(), userId);
        return ResponseEntity.created(location).body(createdOrder);
    }

   
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable String orderId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("Received request to get order ID: {} for user ID: {}", orderId, userId);

        
        OrderResponse order = orderService.getOrderById(orderId);

        

        log.info("Returning order ID: {} for user ID: {}", orderId, userId);
        return ResponseEntity.ok(order);
    }

   
    @GetMapping("/mine")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to get all orders for user ID: {}", userId);
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        log.info("Returning {} orders for user ID: {}", orders.size(), userId);
        return ResponseEntity.ok(orders);
    }

    
    @PostMapping("/internal/payment-update")
    @PreAuthorize("hasAuthority('SCOPE_INTERNAL_SERVICE') or hasRole('SYSTEM')") 
    public ResponseEntity<OrderResponse> handlePaymentUpdate(
            @Valid @RequestBody PaymentUpdateDto paymentUpdateDto) {
        log.info("Received payment update for order ID: {}", paymentUpdateDto.orderId());
        OrderResponse updatedOrder = orderService.processPaymentUpdate(paymentUpdateDto);
        log.info("Payment update processed for order ID: {}. New status: {}", updatedOrder.id(), updatedOrder.orderStatus());
        return ResponseEntity.ok(updatedOrder);
    }

    
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> cancelMyOrder(
            @PathVariable String orderId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("User {} requesting to cancel order ID: {}", userId, orderId);
        OrderResponse cancelledOrder = orderService.cancelOrder(orderId, userId);
        log.info("Order {} cancelled by user {}. Status: {}", orderId, userId, cancelledOrder.orderStatus());
        return ResponseEntity.ok(cancelledOrder);
    }

   
    @GetMapping("/admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> getAnyOrderByIdForAdmin(@PathVariable String orderId) {
        log.info("Admin request to get order by ID: {}", orderId);
        OrderResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    
    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrdersByUserIdForAdmin(@PathVariable String userId) {
        log.info("Admin request to get all orders for user ID: {}", userId);
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }
}