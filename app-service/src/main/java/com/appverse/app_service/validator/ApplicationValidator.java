package com.appverse.app_service.validator;

import org.springframework.stereotype.Component;

import com.appverse.app_service.client.DeveloperClient;
import com.appverse.app_service.dto.ApplicationRequest;
import com.appverse.app_service.dto.UpdateApplicationRequest;
import com.appverse.app_service.enums.MonetizationType;
import com.appverse.app_service.exception.BadRequestException;
import com.appverse.app_service.exception.CreationException;
import com.appverse.app_service.exception.DuplicateResourceException;
import com.appverse.app_service.exception.ResourceNotFoundException;
import com.appverse.app_service.model.Application;
import com.appverse.app_service.repository.ApplicationRepository;
import com.appverse.app_service.repository.CategoryRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationValidator {

    private final ApplicationRepository applicationRepository;
    private final CategoryRepository categoryRepository;
    private final DeveloperClient developerClient;

    public void validateBasicRequest(ApplicationRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Application name cannot be empty");
        }
        if (applicationRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("An application with this name already exists.");
        }
        categoryRepository.findById(request.categoryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category ID " + request.categoryId() + " not found."));
    }

    public void validateDeveloper(String developerId) {
        if (developerId == null || developerId.isBlank()) {
            throw new BadRequestException("Developer ID is required for application creation.");
        }

        try {
            log.debug("Validating developer ID: {}", developerId);

            boolean exists = developerClient.isDeveloperByKeycloakId(developerId);

            if (!exists) {
                throw new ResourceNotFoundException(
                        "Invalid or non-existent developer ID: " + developerId);
            }

            log.debug("Developer ID {} validated successfully.", developerId);

        } catch (FeignException ex) {
            log.error("Feign failure validating developer {}: {}", developerId, ex.getMessage(), ex);

            throw new BadRequestException(
                    "Developer validation service unavailable or request failed.");
        }
    }

    public void validateMonetizationAndPlans(ApplicationRequest request) {
        MonetizationType type = request.monetizationType();
        boolean hasPlans = request.offeredSubscriptionPlans() != null && !request.offeredSubscriptionPlans().isEmpty();

        if ((type == MonetizationType.FREE || type == MonetizationType.ONE_TIME_PURCHASE) && hasPlans) {
            throw new BadRequestException(
                    "Subscription plans cannot be offered for FREE or purely ONE_TIME_PURCHASE applications through this field. Adjust monetizationType.");
        }

        if ((type == MonetizationType.SUBSCRIPTION || type == MonetizationType.ONE_TIME_OR_SUBSCRIPTION) && !hasPlans) {
            log.warn(
                    "Application monetizationType indicates subscription, but no offeredSubscriptionPlans provided for app: {}",
                    request.name());
        }
    }

    public void validateUpdateRequest(UpdateApplicationRequest request, Application existingApp) {
        if (request.name() != null && !existingApp.getName().equalsIgnoreCase(request.name())
                && applicationRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("An application with name '" + request.name() + "' already exists.");
        }
        if (request.categoryId() != null) {
            categoryRepository.findById(request.categoryId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Category ID " + request.categoryId() + " not found."));
        }
    }

}
