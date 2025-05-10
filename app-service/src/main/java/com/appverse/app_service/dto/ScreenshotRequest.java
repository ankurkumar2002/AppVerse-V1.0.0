package com.appverse.app_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL; // Or jakarta equivalent

// DTO for Screenshot data within a request (if needed separately from model)
public record ScreenshotRequest(
    @NotBlank
    @URL
    @Size(max = 1024) // Example max length
    String imageUrl,

    @Size(max = 255) // Caption is optional
    String caption,

    @Min(0) // Order should be non-negative
    Integer order // Optional depending on if user provides it
) {}