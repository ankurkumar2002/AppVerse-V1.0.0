// File: com/appverse/subscription_service/event/payload/SubscriptionRequiresPaymentActionPayload.java
package com.appverse.subscription_service.event.payload;

import java.time.Instant;

public record SubscriptionRequiresPaymentActionPayload(
    String subscriptionId,
    String userId,
    String planId,
    String clientSecret, // From payment gateway for frontend to handle 3DS etc.
    String message,      // e.g., "Payment requires further user action."
    Instant eventTimestamp
) {}