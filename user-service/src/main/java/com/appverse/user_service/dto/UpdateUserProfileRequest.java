package com.appverse.user_service.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    String phone

) {}
