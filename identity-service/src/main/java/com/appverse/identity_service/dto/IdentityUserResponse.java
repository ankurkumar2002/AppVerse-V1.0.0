package com.appverse.identity_service.dto;


public record IdentityUserResponse(
        String id,
        String username,
        String email,
        Boolean emailVerified
) {}
