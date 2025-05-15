// === In app-service Project ===
package com.appverse.app_service.services.createService;

// ... imports ...
import com.appverse.app_service.model.Application;

import org.springframework.stereotype.Component;

import com.appverse.app_service.dto.ApplicationRequest;

@Component
public class ApplicationCreateService {
    public Application toEntity(ApplicationRequest request) {
        // ... (consistency logic for isFree, price, monetizationType as before) ...
        boolean derivedIsFree = request.isFree();
        java.math.BigDecimal actualPrice = request.price();
        // ... (your consistency logic here) ...

        return Application.builder()
                .name(request.name())
                .tagline(request.tagline())
                .description(request.description())
                .version(request.version())
                .categoryId(request.categoryId())
                .price(actualPrice)
                .currency(request.currency())
                .isFree(derivedIsFree)
                .monetizationType(request.monetizationType())
                // offeredSubscriptionPlans from request will be handled in ApplicationServiceImpl
                // applicationSpecificSubscriptionPlanIds will be populated in ApplicationServiceImpl after calling subscription-service
                .platforms(request.platforms())
                .accessUrl(request.accessUrl())
                .websiteUrl(request.websiteUrl())
                .supportUrl(request.supportUrl())
                .developerId(request.developerId())
                .tags(request.tags())
                .status(request.status())
                .publishedAt(request.publishedAt())
                .build();
    }
}