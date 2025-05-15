package com.appverse.app_service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.hibernate.validator.constraints.URL;

import com.appverse.app_service.enums.MonetizationType;
import com.appverse.app_service.model.Screenshot;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record UpdateApplicationRequest(

    @NotBlank String name,
    @Size(max = 255) String tagline,
    @NotBlank String description,
    @NotBlank String version,
    @NotBlank String categoryId,
    @NotNull @DecimalMin("0.0") BigDecimal price,
    String currency,
    boolean isFree,
    @NotEmpty List<@NotBlank String> platforms,
    @NotBlank @URL String accessUrl,
    @URL String websiteUrl,
    @URL String supportUrl,
    @NotBlank @URL String thumbnailUrl,
    @NotNull @Valid List<Screenshot> screenshots,
    @NotBlank String developerId,
    List<@NotBlank String> tags,
    @NotBlank String status,
    Instant publishedAt,
    MonetizationType monetizationType, // Allow updating this (might be nullable if not always updated)
    List<String> associatedSubscriptionPlanIds // Allow updating this

) {}
