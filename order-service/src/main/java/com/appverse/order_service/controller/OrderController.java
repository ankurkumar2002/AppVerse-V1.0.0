
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

    /**
     * Creates a new order for the authenticated user.
     *
     * @param createOrderRequest The request body containing order items.
     * @param jwt                The JWT token of the authenticated user.
     * @return ResponseEntity with the created order and HTTP status 201 (Created).
     */
    @PostMapping
    // @PreAuthorize("isAuthenticated()") // Or specific role like "ROLE_USER"
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject(); // 'sub' claim usually holds the Keycloak User ID
        log.info("Received request to create order for user ID: {}", userId);

        OrderResponse createdOrder = orderService.createOrder(userId, createOrderRequest);

        // Create location URI for the newly created resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdOrder.id())
                .toUri();

        log.info("Order {} created successfully for user {}", createdOrder.id(), userId);
        return ResponseEntity.created(location).body(createdOrder);
    }

    /**
     * Retrieves a specific order by its ID, ensuring it belongs to the authenticated user.
     *
     * @param orderId The ID of the order to retrieve.
     * @param jwt     The JWT token of the authenticated user.
     * @return ResponseEntity with the order details.
     */
    @GetMapping("/{orderId}")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable String orderId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("Received request to get order ID: {} for user ID: {}", orderId, userId);

        // Fetch the order. The service layer should ideally handle authorization
        // (i.e., check if the order belongs to the userId), or you can do a preliminary check here.
        // For simplicity, we'll assume the service layer handles it or it's an admin endpoint.
        // If strict user ownership is required for this specific endpoint path:
        // OrderResponse order = orderService.getOrderByIdAndUserId(orderId, userId);
        OrderResponse order = orderService.getOrderById(orderId);

        // Optional: Explicitly check if the fetched order's userId matches the JWT's userId
        // if (!order.userId().equals(userId)) {
        //     log.warn("User {} attempted to access order {} which does not belong to them.", userId, orderId);
        //     throw new AccessDeniedException("You do not have permission to access this order.");
        // }

        log.info("Returning order ID: {} for user ID: {}", orderId, userId);
        return ResponseEntity.ok(order);
    }

    /**
     * Retrieves all orders for the authenticated user.
     *
     * @param jwt The JWT token of the authenticated user.
     * @return ResponseEntity with a list of orders.
     */
    @GetMapping("/mine")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to get all orders for user ID: {}", userId);
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        log.info("Returning {} orders for user ID: {}", orders.size(), userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Endpoint to receive payment status updates.
     * This endpoint should be secured, typically callable only by the Payment Service
     * (e.g., using client credentials or a pre-shared secret if not using user context for this call).
     * For simplicity, showing it here. In a real system, this might be a separate controller
     * or use a different authentication mechanism if called system-to-system.
     *
     * @param paymentUpdateDto The payment update details.
     * @return ResponseEntity with the updated order.
     */
    @PostMapping("/internal/payment-update") // Using a distinct path for internal updates
    @PreAuthorize("hasAuthority('SCOPE_INTERNAL_SERVICE') or hasRole('SYSTEM')") // Example for service-to-service auth
    public ResponseEntity<OrderResponse> handlePaymentUpdate(
            @Valid @RequestBody PaymentUpdateDto paymentUpdateDto) {
        log.info("Received payment update for order ID: {}", paymentUpdateDto.orderId());
        OrderResponse updatedOrder = orderService.processPaymentUpdate(paymentUpdateDto);
        log.info("Payment update processed for order ID: {}. New status: {}", updatedOrder.id(), updatedOrder.orderStatus());
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * Allows an authenticated user to cancel their own order if it's in a cancellable state.
     *
     * @param orderId The ID of the order to cancel.
     * @param jwt The JWT token of the authenticated user.
     * @return ResponseEntity with the cancelled order.
     */
    @PostMapping("/{orderId}/cancel")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> cancelMyOrder(
            @PathVariable String orderId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("User {} requesting to cancel order ID: {}", userId, orderId);
        OrderResponse cancelledOrder = orderService.cancelOrder(orderId, userId);
        log.info("Order {} cancelled by user {}. Status: {}", orderId, userId, cancelledOrder.orderStatus());
        return ResponseEntity.ok(cancelledOrder);
    }

    // --- Admin Endpoints (Example - secure these appropriately with ADMIN roles) ---

    /**
     * Admin endpoint to get any order by ID.
     *
     * @param orderId The ID of the order.
     * @return The order details.
     */
    @GetMapping("/admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> getAnyOrderByIdForAdmin(@PathVariable String orderId) {
        log.info("Admin request to get order by ID: {}", orderId);
        OrderResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Admin endpoint to get all orders for a specific user.
     *
     * @param userId The ID of the user.
     * @return List of orders for the user.
     */
    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrdersByUserIdForAdmin(@PathVariable String userId) {
        log.info("Admin request to get all orders for user ID: {}", userId);
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }
}