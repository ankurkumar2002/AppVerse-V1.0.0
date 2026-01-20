package com.appverse.developer_service.dto;

import java.time.Instant;

import com.appverse.developer_service.enums.DeveloperStatus;
import com.appverse.developer_service.enums.DeveloperType;

public record DeveloperResponse(
    String id,

    // identity-derived (read-only)
    String username,
    String firstName,
    String lastName,
    String email,

    // developer-specific
    String website,
    String companyName,
    String bio,
    String logoUrl,
    String location,

    DeveloperStatus status,
    DeveloperType developerType,
    boolean isVerified,

    Instant createdAt
) {}
