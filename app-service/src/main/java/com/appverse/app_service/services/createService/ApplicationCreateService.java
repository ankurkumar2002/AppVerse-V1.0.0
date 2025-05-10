package com.appverse.app_service.services.createService;

import org.springframework.stereotype.Component;

import com.appverse.app_service.dto.ApplicationRequest;
import com.appverse.app_service.model.Application;

@Component
public class ApplicationCreateService {

    public Application toEntity(ApplicationRequest request) {
        return Application.builder()
                .name(request.name())
                .tagline(request.tagline())
                .description(request.description())
                .version(request.version())
                .categoryId(request.categoryId())
                .price(request.price())
                .currency(request.currency())
                .isFree(request.isFree())
                .platforms(request.platforms())
                .accessUrl(request.accessUrl())
                .websiteUrl(request.websiteUrl())
                .supportUrl(request.supportUrl())
                .thumbnailUrl(request.thumbnailUrl())
                .developerId(request.developerId())
                .tags(request.tags())
                .status(request.status())
                .build();
    }
}
