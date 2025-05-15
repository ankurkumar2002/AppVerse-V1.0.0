package com.appverse.payment_service.client; // Or your client package


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    ResponseEntity<OrderServiceResponse> updateOrderStatus(@RequestBody PaymentUpdateDtoForOrderService paymentUpdate);
}