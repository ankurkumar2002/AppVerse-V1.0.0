// === In Payment Service Project ===
package com.appverse.payment_service.services;

import com.appverse.payment_service.dto.*;
import com.appverse.payment_service.enums.PaymentGatewayType;

import java.util.List;

public interface PaymentService {

    /**
     * Initiates a payment process.
     * This would involve creating a PaymentTransaction record and interacting with the payment gateway.
     * The response might include details needed for frontend (e.g., client secret for Stripe, redirect URL for PayPal).
     */
    PaymentResponse initiatePayment(InitiatePaymentRequest request);

    /**
     * Updates the status of a payment transaction.
     * Typically called by a webhook handler from the payment gateway or an internal process.
     */
    PaymentResponse updatePaymentStatus(PaymentStatusUpdateRequest request);

    /**
     * Retrieves a payment transaction by its internal ID.
     */
    PaymentResponse getPaymentTransactionById(String paymentTransactionId);

    /**
     * Retrieves payment transactions for a specific user.
     */
    List<PaymentResponse> getPaymentTransactionsByUserId(String userId);

    /**
     * Retrieves payment transactions for a specific reference (e.g., all payments for an order).
     */
    List<PaymentResponse> getPaymentTransactionsByReferenceId(String referenceId);


    // --- Stored Payment Methods ---

    /**
     * Adds a new stored payment method for a user.
     * This involves taking a token from the frontend (obtained from the gateway's SDK)
     * and using the gateway's API to save it (e.g., creating a PaymentMethod and attaching to a Customer in Stripe).
     *
     * @param userId The ID of the user owning this payment method.
     * @param request Details of the payment method to add (containing the gateway token).
     * @return The details of the stored payment method.
     */
    StoredPaymentMethodResponse addStoredPaymentMethod(String userId, AddStoredPaymentMethodRequest request);

    /**
     * Lists all stored payment methods for a given user and gateway.
     *
     * @param userId The ID of the user.
     * @param gateway (Optional) Filter by specific gateway.
     * @return A list of stored payment methods.
     */
    List<StoredPaymentMethodResponse> listStoredPaymentMethods(String userId, PaymentGatewayType gateway);

    /**
     * Deletes/detaches a stored payment method for a user.
     * This involves calling the payment gateway's API to detach/delete the method.
     *
     * @param userId The ID of the user.
     * @param storedPaymentMethodId The internal ID of the stored payment method to delete.
     */
    void deleteStoredPaymentMethod(String userId, String storedPaymentMethodId);

    /**
     * Sets a stored payment method as the default for a user for a specific gateway.
     *
     * @param userId The ID of the user.
     * @param storedPaymentMethodId The internal ID of the stored payment method.
     */
    StoredPaymentMethodResponse setStoredPaymentMethodAsDefault(String userId, String storedPaymentMethodId);

    // TODO: Refund methods would go here later
    // RefundResponse processRefund(InitiateRefundRequest request);
}