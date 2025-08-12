// src/main/java/com/appverse/user_service/dto/RoleAssignRequest.java
package com.appverse.user_service.dto;

import com.appverse.user_service.enums.Role;

public record RoleAssignRequest(
    String keycloakUserId,
    Role role
) {}
