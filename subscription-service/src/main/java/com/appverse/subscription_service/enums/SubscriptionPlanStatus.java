package com.appverse.subscription_service.enums;
public enum SubscriptionPlanStatus {
    ACTIVE,     // Plan is available for new subscriptions
    INACTIVE,   // Plan is not available for new subscriptions, existing ones may continue or be migrated
    ARCHIVED    // Plan is retired and no longer in use
}