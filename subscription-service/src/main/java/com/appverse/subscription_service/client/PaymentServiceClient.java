// === In Subscription Service Project ===
package com.appverse.subscription_service.client;

import com.appverse.subscription_service.enums.PaymentGatewayType; // Assuming this enum exists in a shared lib or also here
import com.appverse.subscription_service.enums.PaymentReferenceType; // Assuming this enum exists in a shared lib or also here

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.annotation.Observed;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "payment-service" , url = "${feign.client.payment-service.url}" )
public interface PaymentServiceClient {

    // DTO to initiate a payment (matches PaymentService's InitiatePaymentRequest)
    record InitiatePaymentPayload(
        String userId,
        String referenceId, // This will be UserSubscription.id
        PaymentReferenceType referenceType, // SUBSCRIPTION_INITIAL or SUBSCRIPTION_RENEWAL
        BigDecimal amount,
        String currency,
        PaymentGatewayType paymentGateway,
        String description,
        String paymentMethodToken, // From user's new card via frontend SDK
        String customerId,         // Gateway's customer ID
        String storedPaymentMethodId, // If using an existing saved payment method ID
        Map<String, Object> metadata
    ) {}

    // DTO for the response from payment-service (matches PaymentService's PaymentResponse)
    // It's good to have specific DTOs for Feign clients to decouple from the exact DTOs of the target service,
    // but for simplicity, we can assume it's similar enough or map it.
    record PaymentServiceResponse(
        String id, // This is the paymentTransactionId
        String status, // e.g., "SUCCEEDED", "FAILED", "REQUIRES_ACTION" (as String)
        String clientSecret, // For Stripe.js if needed
        String gatewayPaymentIntentId,
        String gatewayTransactionId
        // ... other fields you might need from payment-service's response
    ) {}

    @PostMapping("/api/v1/payments/initiate")
    @CircuitBreaker(name = "paymentServiceClient", fallbackMethod = "initiatePaymentFallback")
    @Retry(name = "paymentServiceClient") // Uncomment if you want to add retry logic
    @Observed(name = "subscriptionService.initiatepayment", contextualName = "InitiatePayment")
    ResponseEntity<PaymentServiceResponse> initiatePayment(@RequestBody InitiatePaymentPayload payload);

    // Fallback method for initiatePayment
    default ResponseEntity<PaymentServiceResponse> initiatePaymentFallback(
            InitiatePaymentPayload payload, Throwable throwable) {
        // Log the error and return a default response
        System.out.println("Fallback for initiatePayment: " + throwable.getMessage());
        return ResponseEntity.ok(new PaymentServiceResponse(
                payload.referenceId,
                "FAILED",
                null,
                null,
                null
        ));
    }

    // You might also need a client method to add/manage stored payment methods
    // if SubscriptionService orchestrates that part for the user during signup.
    // For example:
    // record AddStoredPaymentMethodPayload(...)
    // record StoredPaymentMethodDetails(...)
    // @PostMapping("/api/v1/payments/methods")
    // ResponseEntity<StoredPaymentMethodDetails> addStoredPaymentMethod(
    //     @RequestHeader("Authorization") String bearerToken, // If called as the user
    //     @RequestBody AddStoredPaymentMethodPayload payload
    // );
}