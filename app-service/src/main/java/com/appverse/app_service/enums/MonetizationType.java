package com.appverse.app_service.enums;


public enum MonetizationType {
    FREE,                       // Always free, price is 0
    ONE_TIME_PURCHASE,          // Purchased once for a specific price
    SUBSCRIPTION_ONLY,          // Access only granted via an active subscription (price field in Application model might be 0 or informational)
    ONE_TIME_OR_SUBSCRIPTION    // Can be bought outright OR accessed via a subscription
}