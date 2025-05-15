package com.appverse.payment_service.enums;
public enum PaymentMethodType {
    CARD,
    BANK_ACCOUNT, // e.g., ACH, SEPA
    DIGITAL_WALLET, // e.g., PayPal Balance, Apple Pay, Google Pay (if integrated that way)
    IDEAL,
    SOFORT,
    // Add more as needed based on gateway integrations
    UNKNOWN
}