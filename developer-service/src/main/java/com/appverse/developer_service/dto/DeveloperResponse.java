package com.appverse.developer_service.dto;


import java.time.Instant; // Use correct timestamp type

import com.appverse.developer_service.enums.DeveloperStatus;
import com.appverse.developer_service.enums.DeveloperType;

/**
 * DTO representing the Developer profile data returned by the API.
 */
public record DeveloperResponse(
    String id, // Use Long to match the Entity ID type
    String name,
    String email,
    String website,
    String companyName,
    String bio,
    String logoUrl,
    String location,
    DeveloperStatus status, // Expose status
    DeveloperType developerType, // Expose type
    boolean isVerified, // Expose verification status
    Instant createdAt // Expose creation timestamp
    // updatedAt is often less relevant for public responses
) {}