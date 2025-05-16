// === In developer-service Project ===
package com.appverse.developer_service.event.payload;

import com.appverse.developer_service.enums.DeveloperType;
import java.time.Instant;

public record DeveloperProfileUpdatedPayload(
    String developerId,
    String keycloakUserId, // Usually not updatable, but good for context
    String name,
    String email,
    DeveloperType developerType,
    String website,     // Example of other updatable fields
    String companyName,
    String bio,
    String logoUrl,
    String location,
    Instant updatedAt // Assuming your Developer entity has this
    // You might include a list of changed fields if consumers need that level of detail
) {}