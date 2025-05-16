// === In notification-service Project ===
package com.appvese.notification_service.payload; // Or your DTO/event package

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

// This DTO should match the structure of the JSON payload published by app-service
// for application creation events.
public record ApplicationCreatedNotificationPayload(
    String id,
    String name,
    String developerId, // You might want to fetch developer's email using this
    // MonetizationType monetizationType, // Might be useful for email content
    BigDecimal price,
    String currency,
    boolean isFree,
    Instant createdAt
    // Add any other fields from the app-service's ApplicationCreatedPayload
    // that you want to use in the email.
) {}