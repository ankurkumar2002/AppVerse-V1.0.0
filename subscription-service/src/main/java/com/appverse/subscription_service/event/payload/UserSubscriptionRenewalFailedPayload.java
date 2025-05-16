// === In subscription-service Project ===
package com.appverse.subscription_service.event.payload;

import com.appverse.subscription_service.enums.UserSubscriptionStatus;
import java.time.Instant;

public record UserSubscriptionRenewalFailedPayload(
    String subscriptionId,
    String userId,
    String planId,
    String paymentTransactionId, // ID of the failed payment attempt (if available)
    UserSubscriptionStatus newStatus, // e.g., PAST_DUE
    Instant failureTimestamp
) {}