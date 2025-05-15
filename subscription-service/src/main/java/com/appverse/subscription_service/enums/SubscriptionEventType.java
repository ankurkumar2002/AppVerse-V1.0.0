package com.appverse.subscription_service.enums;
public enum SubscriptionEventType {
    CREATED,                // UserSubscription record initiated
    TRIAL_STARTED,
    ACTIVATED,              // Became active (e.g., after trial or successful initial payment)
    RENEWAL_SCHEDULED,
    RENEWAL_ATTEMPTED,
    RENEWAL_SUCCESSFUL,
    RENEWAL_FAILED,
    PAYMENT_METHOD_UPDATED,
    CANCELLED_BY_USER,
    CANCELLED_BY_SYSTEM,    // e.g., due to repeated payment failures
    UNPAID,                 // Moved to unpaid state
    EXPIRED,
    PAUSED,
    RESUMED,
    PLAN_CHANGED,           // Upgraded or downgraded
    STATUS_CHANGED          // Generic status change with details
}