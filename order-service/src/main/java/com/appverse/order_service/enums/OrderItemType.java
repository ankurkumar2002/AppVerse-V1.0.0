package com.appverse.order_service.enums;

public enum OrderItemType {
    ONE_TIME_PURCHASE,
    SUBSCRIPTION_INITIAL_PURCHASE, // For the first payment of a new subscription
    // SUBSCRIPTION_RENEWAL will likely be handled by subscription service directly with payment service
}