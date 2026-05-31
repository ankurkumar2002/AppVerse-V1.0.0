package com.appverse.user_service.dto;

public record MessageResponse (
    String message,
    String kyeycloakUserId
){}
