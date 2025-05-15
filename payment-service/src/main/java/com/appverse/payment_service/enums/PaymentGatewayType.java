package com.appverse.payment_service.enums;
public enum PaymentGatewayType {
    STRIPE,
    PAYPAL,
    // BRAINTREE, // Example for future
    // ADYEN,     // Example for future
    INTERNAL_CREDIT, // If you implement a wallet/credit system
    MANUAL           // For manual/offline payments recorded by an admin
}