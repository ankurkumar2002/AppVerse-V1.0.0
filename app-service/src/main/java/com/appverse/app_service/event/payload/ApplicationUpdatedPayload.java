
package com.appverse.app_service.event.payload;

import com.appverse.app_service.enums.MonetizationType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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
    Instant updatedAt
   
) {}
