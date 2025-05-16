package com.appverse.payment_service.client; // Or your client package


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.annotation.Observed;

@FeignClient(name = "order-service", url = "${feign.client.order-service.url}")
public interface OrderServiceClient {

    // DTO for sending payment update to order-service
    // Should match what OrderService's POST /internal/payment-update expects
    record PaymentUpdateDtoForOrderService(
        String orderId,
        String paymentTransactionId,
        String paymentStatus, // Use String here to avoid direct enum dependency if services are very separate
                              // OrderService will parse this string into its own PaymentStatus enum
        String failureReason
    ) {}

    // DTO for the response from order-service (simplified)
    record OrderServiceResponse(String id, String orderStatus) {}


    @PostMapping("/api/v1/orders/internal/payment-update") // Matches OrderController endpoint
    @CircuitBreaker(name = "orderServiceClient", fallbackMethod = "updateOrderStatusFallback")
    @Retry(name = "orderServiceClient") // Uncomment if you want to add retry logic
    @Observed(name = "paymentService.UpdateOrderStatus", contextualName = "Update Order Status")
    ResponseEntity<OrderServiceResponse> updateOrderStatus(@RequestBody PaymentUpdateDtoForOrderService paymentUpdate);

    // Fallback method for updateOrderStatus
    default ResponseEntity<OrderServiceResponse> updateOrderStatusFallback(
            PaymentUpdateDtoForOrderService paymentUpdate, Throwable throwable) {
        // Log the error and return a default response
        System.out.println("Fallback for updateOrderStatus: " + throwable.getMessage());
        return ResponseEntity.ok(new OrderServiceResponse(paymentUpdate.orderId(), "FAILED"));
    }
}