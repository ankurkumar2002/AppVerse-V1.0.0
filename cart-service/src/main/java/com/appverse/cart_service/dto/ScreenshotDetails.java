package com.appverse.cart_service.dto;

public record ScreenshotDetails( // Assuming ScreenshotResponse has these fields
        String id,
        String imageUrl,
        String caption,
        Integer order // or int
    ) {}