package com.appverse.app_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL; 

public record ScreenshotRequest(
    @NotBlank
    @URL
    @Size(max = 1024) 
    String imageUrl,

    @Size(max = 255) 
    String caption,

    @Min(0) 
    Integer order 
) {}