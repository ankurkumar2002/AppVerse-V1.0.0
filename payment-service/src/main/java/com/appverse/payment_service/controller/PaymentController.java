// === In Payment Service Project ===
package com.appverse.payment_service.controller;

import com.appverse.payment_service.dto.*;
import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.services.PaymentService; // Corrected import

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Initiates a new payment process.
     * The calling service (e.g., order-service, or frontend orchestrated by order-service)
     * provides necessary details.
     * The response might contain information for the frontend to complete the payment
     * (e.g., a client secret for Stripe Elements, or a redirect URL for PayPal).
     */
    @PostMapping("/initiate")
    // This endpoint might be called by another service or a trusted frontend part
    // So, security could be based on user context or service-to-service auth
    // @PreAuthorize("isAuthenticated() or hasAuthority('SCOPE_INTERNAL_SERVICE')")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest initiatePaymentRequest,
            @AuthenticationPrincipal Jwt jwt) { // Optional: if called by user context

        String userIdForPayment = initiatePaymentRequest.userId();
        if (userIdForPayment == null && jwt != null) {
            userIdForPayment = jwt.getSubject();
        }

        if (userIdForPayment == null) {
            log.error("User ID is missing for payment initiation and not available from JWT.");
            return ResponseEntity.badRequest().build();
        }

        InitiatePaymentRequest finalRequest = new InitiatePaymentRequest(
            userIdForPayment,
            initiatePaymentRequest.referenceId(),
            initiatePaymentRequest.referenceType(),
            initiatePaymentRequest.amount(),
            initiatePaymentRequest.currency(),
            initiatePaymentRequest.paymentGateway(),
            initiatePaymentRequest.description(),
            initiatePaymentRequest.paymentMethodToken(),
            initiatePaymentRequest.customerId(),
            initiatePaymentRequest.metadata()
        );

        log.info("Received request to initiate payment for user ID: {}, reference ID: {}",
                finalRequest.userId(), finalRequest.referenceId());
        PaymentResponse paymentResponse = paymentService.initiatePayment(finalRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/transactions/{id}")
                .buildAndExpand(paymentResponse.id())
                .toUri();

        return ResponseEntity.created(location).body(paymentResponse);
    }

    /**
     * Endpoint to receive payment status updates, typically from gateway webhooks.
     * IMPORTANT: This endpoint MUST be secured robustly (e.g., by verifying webhook signatures).
     * This is a simplified version; a real webhook handler is more complex.
     */
    @PostMapping("/webhook/{gateway}")
    public ResponseEntity<Void> handleGatewayWebhook(
            @PathVariable String gateway,
            @RequestBody String rawPayload,
            @RequestHeader Map<String, String> headers) {
        log.info("Received webhook from gateway: {}", gateway);
        // 1. Verify webhook signature (CRITICAL FOR SECURITY)
        //    - Stripe: Stripe.Event.constructEvent(rawPayload, headers.get("Stripe-Signature"), stripeWebhookSecret)
        //    - PayPal: Verify using PayPal SDK and credentials/webhook ID.

        // 2. Parse the rawPayload into a gateway-specific event object.

        // 3. Extract necessary information and map to your PaymentStatusUpdateRequest DTO.
        //    PaymentStatusUpdateRequest updateRequest = ... map from gateway event ...

        // 4. Call the service to update the payment status.
        //    paymentService.updatePaymentStatus(updateRequest);

        log.warn("Webhook handling for gateway {} is a placeholder. Implement signature verification and payload processing.", gateway);
        return ResponseEntity.ok().build();
    }

    // ===================================================================================
    // == MOCK ENDPOINT FOR TESTING PAYMENT STATUS UPDATES (Development/Testing Only) ==
    // ===================================================================================
    /**
     * MOCK Endpoint: Manually triggers a payment status update for testing purposes.
     * In a real system, this kind of update would come from a verified gateway webhook.
     * SECURE THIS ENDPOINT APPROPRIATELY if kept for testing environments (e.g., ADMIN role or specific internal scope).
     *
     * @param request The PaymentStatusUpdateRequest DTO.
     * @return ResponseEntity with the updated payment details.
     */
    @PostMapping("/internal/mock-payment-update")
    // Example Security: Ensure only admins or test systems can call this.
    // @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_TEST_UTILITIES')")
    public ResponseEntity<PaymentResponse> mockPaymentUpdate(
            @Valid @RequestBody PaymentStatusUpdateRequest request) {
        log.warn("MOCK PAYMENT UPDATE: Received mock update for transaction ID: {} to status: {}. Ensure this is only used in test environments.",
                request.paymentTransactionId(), request.newStatus());

        PaymentResponse updatedPayment = paymentService.updatePaymentStatus(request);
        log.info("MOCK PAYMENT UPDATE: Processed for transaction ID: {}. New status: {}", updatedPayment.id(), updatedPayment.status());
        return ResponseEntity.ok(updatedPayment);
    }
    // ===================================================================================
    // == END OF MOCK ENDPOINT                                                          ==
    // ===================================================================================


    /**
     * Retrieves a specific payment transaction by its ID.
     */
    @GetMapping("/transactions/{paymentTransactionId}")
    public ResponseEntity<PaymentResponse> getPaymentTransaction(
            @PathVariable String paymentTransactionId,
            @AuthenticationPrincipal Jwt jwt) {
        // TODO: Add logic to ensure user owns this transaction or is an admin
        log.info("Request to get payment transaction ID: {}", paymentTransactionId);
        PaymentResponse payment = paymentService.getPaymentTransactionById(paymentTransactionId);
        return ResponseEntity.ok(payment);
    }

    /**
     * Retrieves all payment transactions for the authenticated user.
     */
    @GetMapping("/transactions/mine")
    public ResponseEntity<List<PaymentResponse>> getMyPaymentTransactions(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Request to get payment transactions for user ID: {}", userId);
        List<PaymentResponse> transactions = paymentService.getPaymentTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    // --- Stored Payment Methods Endpoints ---

    @PostMapping("/methods")
    public ResponseEntity<StoredPaymentMethodResponse> addStoredPaymentMethod(
            @Valid @RequestBody AddStoredPaymentMethodRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Request to add stored payment method for user ID: {} via gateway: {}", userId, request.paymentGateway());
        StoredPaymentMethodResponse response = paymentService.addStoredPaymentMethod(userId, request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}") // This should probably be /methods/{id}
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/methods/mine")
    public ResponseEntity<List<StoredPaymentMethodResponse>> listMyStoredPaymentMethods(
            @RequestParam(required = false) PaymentGatewayType gateway,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Request to list stored payment methods for user ID: {}, gateway: {}", userId, gateway);
        List<StoredPaymentMethodResponse> methods = paymentService.listStoredPaymentMethods(userId, gateway);
        return ResponseEntity.ok(methods);
    }

    @DeleteMapping("/methods/{storedPaymentMethodId}")
    public ResponseEntity<Void> deleteMyStoredPaymentMethod(
            @PathVariable String storedPaymentMethodId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Request to delete stored payment method ID: {} for user ID: {}", storedPaymentMethodId, userId);
        paymentService.deleteStoredPaymentMethod(userId, storedPaymentMethodId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/methods/{storedPaymentMethodId}/default")
    public ResponseEntity<StoredPaymentMethodResponse> setMyStoredPaymentMethodAsDefault(
            @PathVariable String storedPaymentMethodId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Request to set stored payment method ID: {} as default for user ID: {}", storedPaymentMethodId, userId);
        StoredPaymentMethodResponse response = paymentService.setStoredPaymentMethodAsDefault(userId, storedPaymentMethodId);
        return ResponseEntity.ok(response);
    }
}