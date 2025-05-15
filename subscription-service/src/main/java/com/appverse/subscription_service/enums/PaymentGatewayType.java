// === In payment-service (e.g., com.appverse.payment_service.enums) ===
// === OR in a shared library ===
package com.appverse.subscription_service.enums; // Adjust package if in shared lib

public enum PaymentGatewayType {
    STRIPE,
    PAYPAL,
    BRAINTREE, // Example for future
    ADYEN,     // Example for future
    INTERNAL_CREDIT, // If you implement a wallet/credit system
    MANUAL,          // For manual/offline payments recorded by an admin
    MOCK             // Useful for testing without a real gateway
}