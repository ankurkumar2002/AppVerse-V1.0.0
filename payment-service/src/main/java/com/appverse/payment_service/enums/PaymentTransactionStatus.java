package com.appverse.payment_service.enums;
public enum PaymentTransactionStatus {
    // Initial / In-Progress States
    PENDING_CREATION,           // Transaction record created, about to interact with gateway
    PENDING_GATEWAY_ACTION,     // Waiting for gateway to confirm/process (e.g. user redirected)
    REQUIRES_PAYMENT_METHOD,    // Stripe: PaymentIntent needs a payment method
    REQUIRES_CONFIRMATION,      // Stripe: PaymentIntent needs to be confirmed by client
    REQUIRES_ACTION,            // Stripe: PaymentIntent requires further user action (e.g., 3D Secure)
    PROCESSING,                 // Gateway is actively processing the payment

    // Final States
    SUCCEEDED,                  // Payment was successful
    FAILED,                     // Payment failed
    CANCELLED,                  // Payment was cancelled (by user or system before completion)

    // Post-Success States (related to refunds, without a separate Refund entity for now)
    REFUND_REQUESTED,           // A refund has been requested for this transaction
    REFUND_PROCESSING,          // A refund is being processed by the gateway
    REFUNDED,                   // The entire transaction amount has been refunded
    PARTIALLY_REFUNDED, REQUIRES_CLIENT_ACTION,          // A portion of the transaction amount has been refunded

}