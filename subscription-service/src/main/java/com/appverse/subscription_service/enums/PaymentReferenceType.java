// === In payment-service (e.g., com.appverse.payment_service.enums) ===
// === OR in a shared library ===
package com.appverse.subscription_service.enums; // Adjust package if in shared lib

public enum PaymentReferenceType {
    ORDER,                           // For one-time purchases via order-service
    SUBSCRIPTION_INITIAL,            // Initial payment for a new subscription
    SUBSCRIPTION_RENEWAL,            // Recurring payment for an existing subscription
    DEVELOPER_REGISTRATION_FEE,      // Example: Fee for developers to join platform
    APP_LISTING_FEE,                 // Example: Fee for listing an app
    PLATFORM_SERVICE_CHARGE,         // Example: A general service charge
    WALLET_TOP_UP,                   // Example: If you have a user wallet feature
    REFUND,                          // Could be a type if refunds are initiated as separate transactions
    OTHER                            // Generic catch-all
}