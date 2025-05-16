// === In app-service Project ===
package com.appverse.app_service.event.payload; // Or a sub-package for payloads

import com.appverse.app_service.enums.MonetizationType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

// You might not need ALL fields of the Application. Send what's relevant for consumers.
public record ApplicationCreatedPayload(
    String id,
    String name,
    String developerId,
    String categoryId,
    MonetizationType monetizationType,
    BigDecimal price, // Price for ONE_TIME_PURCHASE
    String currency,
    boolean isFree,
    List<String> platforms,
    String status, // e.g., PUBLISHED, DRAFT
    List<String> tags,
    Instant createdAt,
    List<String> associatedSubscriptionPlanIds // IDs of plans created in subscription-service
                                             // if app-service was responsible for triggering that
                                             // and got them back.
) {}

// The full event would be:
// Event<ApplicationCreatedPayload> where Event is a generic wrapper with EventMetaData
// Or, a specific event class:
// public record ApplicationCreatedEvent(EventMetaData metaData, ApplicationCreatedPayload payload) {}