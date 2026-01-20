package com.appverse.developer_service.dto;

import java.util.List;

public record IdentityUserResponse(

    String id,              // Keycloak userId (sub)
    String username,
    String email,
    boolean emailVerified,
    String firstName,
    String lastName,
    boolean enabled,
    List<String> roles

) {}
