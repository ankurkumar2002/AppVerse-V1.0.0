package com.appverse.app_service.services.createService;

import com.appverse.app_service.model.Application;

import org.springframework.stereotype.Component;

import com.appverse.app_service.dto.ApplicationRequest;
import com.appverse.app_service.enums.ApplicationStatus;

@Component
public class ApplicationCreateService {
    public Application toEntity(ApplicationRequest request, String developerId) {

        Application app = Application.builder()
                .name(request.name())
                .tagline(request.tagline())
                .description(request.description())
                .version(request.version())
                .categoryId(request.categoryId())
                .price(request.price())
                .currency(request.currency())
                .isFree(request.isFree())
                .monetizationType(request.monetizationType())
                .platforms(request.platforms())
                .accessUrl(request.accessUrl())
                .websiteUrl(request.websiteUrl())
                .supportUrl(request.supportUrl())
                .developerId(developerId)
                .tags(request.tags())
                .status(ApplicationStatus.DRAFT)
                .build();

        System.out.println("AFTER BUILD = " + app);

        return app;
    }
}