package com.appverse.order_service.enums;


public enum OrderStatus {
    PENDING_PAYMENT,      // Order created, awaiting payment initiation/completion
    PAYMENT_PROCESSING,   // Payment is actively being processed by the gateway
    PAYMENT_FAILED,       // Payment attempt failed
    AWAITING_FULFILLMENT, // Payment successful, order items need to be fulfilled (e.g., grant access)
    PROCESSING,           // General processing state (could be fulfillment)
    COMPLETED,            // All items fulfilled, order is done
    CANCELLED_BY_USER,
    CANCELLED_BY_SYSTEM,
    REFUND_PENDING,
    REFUNDED,
    PARTIALLY_REFUNDED
}