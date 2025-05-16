// === In app-service Project ===
package com.appverse.app_service.event.payload;

import com.appverse.app_service.enums.MonetizationType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
// import java.util.Set; // If you want to explicitly list changed fields

public record ApplicationUpdatedPayload(
    String id,
    String name,
    String developerId,
    String categoryId,
    MonetizationType monetizationType,
    BigDecimal price,
    String currency,
    boolean isFree,
    List<String> platforms,
    String status,
    List<String> tags,
    Instant updatedAt,
    List<String> associatedSubscriptionPlanIds
    // Optional: A way to indicate which fields changed, if consumers need fine-grained updates
    // Set<String> changedFields,
    // ApplicationSnapshot previousState // For more complex auditing/comparison by consumers
) {}

// Full event:
// public record ApplicationUpdatedEvent(EventMetaData metaData, ApplicationUpdatedPayload payload) {}