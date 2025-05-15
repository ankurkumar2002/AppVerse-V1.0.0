package com.appverse.order_service.enums;

public enum PaymentStatus { // Denormalized from Payment Service for quick checks
    PENDING,
    SUCCEEDED,
    FAILED,
    REFUNDED
}
