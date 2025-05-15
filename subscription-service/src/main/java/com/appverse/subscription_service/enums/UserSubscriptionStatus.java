package com.appverse.subscription_service.enums;
public enum UserSubscriptionStatus {
    PENDING_INITIAL_PAYMENT, // Created, awaiting first payment
    TRIALING,               // In a free trial period
    ACTIVE,                 // Current, paid up
    PAST_DUE,               // Payment failed, in dunning/grace period
    CANCELLED,              // User requested cancellation, active until current period end
    UNPAID,                 // Dunning failed, access revoked
    EXPIRED,                // Non-renewing subscription ended, or cancellation took effect
    INCOMPLETE,             // Initial payment started but requires further action (e.g., 3DS)
    PAUSED,                  // Temporarily paused by user or admin
    SYSTEM_CANCELLED 
}