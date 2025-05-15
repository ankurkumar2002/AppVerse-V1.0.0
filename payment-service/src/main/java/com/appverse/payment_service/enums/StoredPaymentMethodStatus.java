package com.appverse.payment_service.enums;
public enum StoredPaymentMethodStatus {
    ACTIVE,                     // Usable for payments
    INACTIVE,                   // Temporarily disabled by user or system
    EXPIRED,                    // Card has expired
    REMOVED,                    // User removed this payment method
    REQUIRES_VERIFICATION,      // Gateway requires verification (e.g., micro-deposits for bank accounts)
    FAILED_VALIDATION           // Gateway validation failed (e.g., card declined during setup)
}