package com.appverse.app_service.dto;

public record ScreenshotResponse(
    String imageUrl,
    String caption
) {}