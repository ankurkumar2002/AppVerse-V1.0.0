package com.appverse.order_service.enums;

public enum OrderStatus {
    COMPLETED,
    CANCELLED_BY_USER,
    CANCELLED_BY_SYSTEM,
    REFUNDED, AWAITING_FULFILLMENT, PAYMENT_FAILED, PENDING_PAYMENT, PAYMENT_PROCESSING,
    // Add other statuses as needed

    ;

    public boolean isFinal() {
        return this == COMPLETED
            || this == CANCELLED_BY_USER
            || this == CANCELLED_BY_SYSTEM
            || this == REFUNDED;
    }
}
