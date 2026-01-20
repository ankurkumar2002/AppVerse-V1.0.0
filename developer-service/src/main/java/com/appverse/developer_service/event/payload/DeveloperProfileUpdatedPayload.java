package com.appverse.developer_service.event.payload;

import com.appverse.developer_service.enums.DeveloperType;
import java.time.Instant;

public record DeveloperProfileUpdatedPayload(
    String developerId,
    String keycloakUserId,

    // identity context (read-only, not changed here)
    String username,
    String firstName,
    String lastName,
    String email,

    // domain fields that can actually change
    DeveloperType developerType,
    String website,
    String companyName,
    String bio,
    String logoUrl,
    String location,

    Instant updatedAt
) {}
