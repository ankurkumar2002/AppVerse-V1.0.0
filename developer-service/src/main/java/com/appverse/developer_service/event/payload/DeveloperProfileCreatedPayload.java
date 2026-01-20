package com.appverse.developer_service.event.payload;

import com.appverse.developer_service.enums.DeveloperType;
import java.time.Instant;

public record DeveloperProfileCreatedPayload(
    String developerId,        // internal UUID
    String keycloakUserId,     // identity link

    // identity-aligned (explicit)
    String username,
    String firstName,
    String lastName,
    String email,

    // domain-specific
    DeveloperType developerType,
    String companyName,

    Instant createdAt
) {}
