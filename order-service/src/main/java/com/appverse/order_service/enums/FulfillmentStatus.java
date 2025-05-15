package com.appverse.order_service.enums;

public enum FulfillmentStatus {
    PENDING,    // Fulfillment not yet attempted or completed
    SUCCESSFUL, // Item fulfilled (e.g., app access granted)
    FAILED      // Fulfillment attempt failed
}