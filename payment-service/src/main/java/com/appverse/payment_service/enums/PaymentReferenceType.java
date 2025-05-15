package com.appverse.payment_service.enums;
public enum PaymentReferenceType {
    ORDER,                           // For one-time purchases via order-service
    SUBSCRIPTION_INITIAL,            // Initial payment for a new subscription
    SUBSCRIPTION_RENEWAL,            // Recurring payment for an existing subscription
    DEVELOPER_REGISTRATION_FEE,
    APP_LISTING_FEE,
    PLATFORM_SERVICE_CHARGE,
    WALLET_TOP_UP,                   // If you have a user wallet feature
    OTHER
}