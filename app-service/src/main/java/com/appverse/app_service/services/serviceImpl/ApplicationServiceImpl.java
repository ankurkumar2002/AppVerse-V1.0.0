package com.appverse.app_service.services.serviceImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.appverse.app_service.client.SubscriptionServiceClient;

import com.appverse.app_service.dto.ApplicationRequest;
import com.appverse.app_service.dto.ApplicationResponse;
import com.appverse.app_service.dto.DeveloperOfferedSubscriptionPlanDto;
import com.appverse.app_service.dto.MessageResponse;
import com.appverse.app_service.dto.ScreenshotRequest;
import com.appverse.app_service.dto.UpdateApplicationRequest;
import com.appverse.app_service.enums.ApplicationStatus;
import com.appverse.app_service.exception.BadRequestException;
import com.appverse.app_service.exception.CreationException;
import com.appverse.app_service.exception.DatabaseOperationException;
import com.appverse.app_service.exception.ResourceNotFoundException;
import com.appverse.app_service.exception.UpdateOperationException;
import com.appverse.app_service.kafkaEvents.ApplicationEventPublisher;
import com.appverse.app_service.mapper.ApplicationMapper;
import com.appverse.app_service.model.Application;
import com.appverse.app_service.model.Screenshot;
import com.appverse.app_service.repository.ApplicationRepository;
import com.appverse.app_service.services.ApplicationMediaService;
import com.appverse.app_service.services.ApplicationService;
import com.appverse.app_service.services.createService.ApplicationCreateService;
import com.appverse.app_service.validator.ApplicationValidator;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationMediaService applicationMediaService;

    private final ApplicationRepository applicationRepository;
    private final ApplicationCreateService applicationCreateService;
    private final ApplicationMapper applicationMapper;
    private final SubscriptionServiceClient subscriptionServiceClient;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final Executor applicationTaskExecutor;
    private final ApplicationValidator applicationValidator;

    @Override
    @CacheEvict(value = { "applicationById", "allApplications" }, allEntries = true)
    public MessageResponse createApplication(ApplicationRequest request, MultipartFile thumbnail,
            List<MultipartFile> screenshots, List<ScreenshotRequest> metadata, String developerId) {

        log.info("Attempting to create application with name: {}", request.name());

        applicationValidator.validateBasicRequest(request);
        applicationValidator.validateMonetizationAndPlans(request);

        Application application = applicationCreateService.toEntity(request, developerId);
        application.setStatus(ApplicationStatus.DRAFT);
        application.setPublishedAt(null);
        adjustPricingAndFlags(application);
        log.info("Process moving forward");
        CompletableFuture<Void> developerValidationFuture = CompletableFuture.runAsync(() -> {
            try {
                log.info("Async validation task started");

                applicationValidator.validateDeveloper(developerId);

                log.info("Async validation task COMPLETED");
            } catch (Exception e) {
                log.error("Async validation task FAILED", e);
                throw e;
            }
        }, applicationTaskExecutor);

        log.info("Thumbnail filename: {}", thumbnail.getOriginalFilename());

        for (MultipartFile file : screenshots) {
            log.info("Screenshot filename: {}", file.getOriginalFilename());
        }

        String thumbnailUrl = applicationMediaService.uploadThumbnail(thumbnail, request.name());

        List<Screenshot> screenshotEntities = applicationMediaService.uploadScreenshots(screenshots, metadata,
                request.name());

        log.info("Before waitFor");
        waitFor(developerValidationFuture);
        log.info("After waitFor");

        // waitFor(developerValidationFuture, thumbnailUploadFuture,
        // screenshotUploadFuture);

        application.setThumbnailUrl(thumbnailUrl);
        application.setScreenshots(screenshotEntities);

        log.info("Data is going to be pushed");

        log.info(application.toString());

        Application savedApplication = applicationRepository.save(application);

        log.info(savedApplication.toString());

        log.info(savedApplication + " data is pushed");

        // applicationEventPublisher.publishCreated(savedApplication);

        log.info("Application processing complete for ID: {}", savedApplication.getId());
        return new MessageResponse("Application created successfully", savedApplication.getId());
    }

    @SafeVarargs
    private final void waitFor(CompletableFuture<?>... futures) {
        try {
            log.debug("Waiting for {} async tasks to complete...", futures.length);
            CompletableFuture.allOf(futures).join();
            log.debug("All async tasks completed successfully.");
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeEx) {
                throw runtimeEx;
            }
            throw new CreationException("Async task failed during application creation " + cause);
        }
    }

    private void adjustPricingAndFlags(Application application) {

        switch (application.getMonetizationType()) {

            case FREE -> {
                application.setFree(true);
                application.setPrice(BigDecimal.ZERO);
                application.setCurrency("INR");
            }

            case SUBSCRIPTION -> {
                application.setFree(false);
                application.setPrice(BigDecimal.ZERO);
                application.setCurrency("INR");
            }

            case ONE_TIME_PURCHASE, ONE_TIME_OR_SUBSCRIPTION -> {

                if (application.getPrice() == null ||
                        application.getPrice().compareTo(BigDecimal.ZERO) < 0) {

                    throw new BadRequestException(
                            "Price must be provided and non-negative for purchasable monetization types.");
                }

                if (application.getPrice().compareTo(BigDecimal.ZERO) > 0 &&
                        (application.getCurrency() == null ||
                                application.getCurrency().isBlank())) {

                    throw new BadRequestException(
                            "Currency must be provided for priced items.");
                }

                if (application.getCurrency() == null ||
                        application.getCurrency().isBlank()) {
                    application.setCurrency("INR");
                }

                application.setFree(
                        application.getPrice().compareTo(BigDecimal.ZERO) == 0);
            }
        }
    }

    private List<String> handleSubscriptionPlanCreation(ApplicationRequest request, Application savedApplication) {
        List<String> createdPlanIds = new ArrayList<>();
        if (request.offeredSubscriptionPlans() != null && !request.offeredSubscriptionPlans().isEmpty()) {
            for (DeveloperOfferedSubscriptionPlanDto planDto : request.offeredSubscriptionPlans()) {
                var planCreationRequest = new SubscriptionServiceClient.SubscriptionServicePlanCreationRequest(
                        planDto.planNameKey(), planDto.displayName(), planDto.description(), planDto.price(),
                        planDto.currency(), planDto.billingInterval().name(), planDto.billingIntervalCount(),
                        planDto.trialPeriodDays(), savedApplication.getId(), savedApplication.getDeveloperId());

                try {
                    ResponseEntity<SubscriptionServiceClient.SubscriptionServicePlanResponse> response = subscriptionServiceClient
                            .createDeveloperSubscriptionPlan(planCreationRequest);
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        String newPlanId = response.getBody().id();
                        createdPlanIds.add(newPlanId);
                        log.info("Successfully created subscription plan '{}' (ID: {}) for application ID: {}",
                                planDto.displayName(), newPlanId, savedApplication.getId());
                    } else {
                        throw new CreationException(
                                "Failed to create associated subscription plan: " + planDto.displayName()
                                        + ". App creation rolled back. Reason: " + response.getStatusCode());
                    }
                } catch (FeignException ex) {
                    throw new CreationException("Failed to communicate with subscription service for plan: "
                            + planDto.displayName() + ". App creation rolled back." + ex);
                } catch (Exception ex) {
                    throw new CreationException("Unexpected error creating subscription plan: " + planDto.displayName()
                            + ". App creation rolled back." + ex);
                }
            }
        }
        return createdPlanIds;
    }

    // private void updateApplicationWithPlanIds(Application savedApplication) {
    // try {
    // applicationRepository.save(savedApplication);
    // log.info("Updated application {} with {} associated subscription plan IDs.",
    // savedApplication.getId(),
    // savedApplication.getApplicationSpecificSubscriptionPlanIds().size());
    // } catch (DataAccessException e) {
    // throw new DatabaseOperationException("Failed to link subscription plans to
    // application." + e);
    // }
    // }

    @Override
    @CacheEvict(value = { "applicationById", "allApplications" }, allEntries = true)
    @Transactional
    public MessageResponse updateApplication(String id, UpdateApplicationRequest request,
            MultipartFile thumbnail,
            List<MultipartFile> screenshots,
            List<ScreenshotRequest> metadata, String developerId) {

        log.info("Attempting to update application with ID: {}", id);

        Application existingApp = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID " + id + " not found."));

        if (!existingApp.getDeveloperId().equals(developerId)) {
            throw new AccessDeniedException("You do not own this application");
        }

        applicationValidator.validateUpdateRequest(request, existingApp);
        applicationMapper.updateFromDto(request, existingApp);

        CompletableFuture<String> thumbnailUpdateFuture = CompletableFuture.supplyAsync(
                () -> applicationMediaService.updateThumbnail(thumbnail, existingApp.getThumbnailUrl(),
                        existingApp.getName()),
                applicationTaskExecutor);

        CompletableFuture<List<Screenshot>> screenshotUpdateFuture = CompletableFuture.supplyAsync(
                () -> applicationMediaService.updateScreenshots(screenshots, metadata, existingApp.getScreenshots(),
                        existingApp.getName()),
                applicationTaskExecutor);

        try {
            log.debug("Waiting for parallel update tasks to complete for app ID: {}", id);
            CompletableFuture.allOf(thumbnailUpdateFuture, screenshotUpdateFuture).join();
            log.debug("Parallel update tasks completed successfully for app ID: {}", id);
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new UpdateOperationException("An error occurred during a parallel update task." + e.getCause());
        }

        String newThumbnailUrl = thumbnailUpdateFuture.join();
        if (newThumbnailUrl != null) {
            existingApp.setThumbnailUrl(newThumbnailUrl);
        }

        List<Screenshot> newScreenshotEntities = screenshotUpdateFuture.join();
        if (newScreenshotEntities != null) {
            existingApp.getScreenshots().clear();
            existingApp.getScreenshots().addAll(newScreenshotEntities);
        }

        Application updatedApp = applicationRepository.save(existingApp);
        log.info("Application {} updated successfully in database.", updatedApp.getId());

        // applicationEventPublisher.publishUpdated(updatedApp);

        log.info("Application {} update processing complete.", updatedApp.getId());
        return new MessageResponse("Application Updated Successfully!", updatedApp.getId());
    }

    @Override
    @CacheEvict(value = { "applicationById", "allApplications" }, allEntries = true)
    @Transactional
    public void deleteApplication(String id, String developerId) {
        log.info("Attempting to delete application with ID: {}", id);
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application with ID " + id + " not found, cannot delete."));
        if (!application.getDeveloperId().equals(developerId)) {
            throw new AccessDeniedException("You do not own this application");
        }

        applicationRepository.deleteById(id);
        // applicationEventPublisher.publishDeleted(application);
    }

    @Override
    @Cacheable(value = "applicationById", key = "#id")
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(String id) {
        log.debug("Fetching application by ID: {}", id);
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));
        return applicationMapper.toResponse(application);
    }

    @Override
    @Cacheable("allApplications")
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications() {
        log.debug("Fetching all applications.");
        List<Application> applications = applicationRepository.findAll();
        return applicationMapper.toResponseList(applications);
    }

    @Override
    @CacheEvict(value = { "applicationById", "allApplications" }, allEntries = true)
    @Transactional
    public MessageResponse updateApplicationStatus(
            String appId,
            ApplicationStatus newStatus,
            String developerId) {

        log.info("Updating status of application {} to {}", appId, newStatus);

        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with ID: " + appId));

        if (!app.getDeveloperId().equals(developerId)) {
            throw new AccessDeniedException("You do not own this application");
        }

        validateStatusTransition(app.getStatus(), newStatus);

        app.setStatus(newStatus);

        if (newStatus == ApplicationStatus.PUBLISHED) {
            validateReadyForPublish(app);

            if (app.getPublishedAt() == null) {
                app.setPublishedAt(Instant.now());
            }
        }

        applicationRepository.save(app);

        log.info("Application {} status updated to {}", appId, newStatus);

        return new MessageResponse(
                "Application status updated to " + newStatus,
                appId);
    }

    private void validateStatusTransition(
            ApplicationStatus current,
            ApplicationStatus target) {

        if (current == target) {
            throw new BadRequestException(
                    "Application is already in status: " + target);
        }

        switch (current) {
            case DRAFT -> {
                if (target != ApplicationStatus.PUBLISHED &&
                        target != ApplicationStatus.ARCHIVED) {
                    throw new BadRequestException(
                            "Invalid transition from DRAFT to " + target);
                }
            }

            case PUBLISHED -> {
                if (target != ApplicationStatus.UNPUBLISHED &&
                        target != ApplicationStatus.ARCHIVED) {
                    throw new BadRequestException(
                            "Invalid transition from PUBLISHED to " + target);
                }
            }

            case UNPUBLISHED -> {
                if (target != ApplicationStatus.PUBLISHED &&
                        target != ApplicationStatus.ARCHIVED) {
                    throw new BadRequestException(
                            "Invalid transition from UNPUBLISHED to " + target);
                }
            }
        }
    }

    private void validateReadyForPublish(Application app) {
        log.info("Checking publish readiness");
        if (app.getThumbnailUrl() == null || app.getThumbnailUrl().isBlank()) {
            log.error("Thumbnail missing");
            throw new BadRequestException("Thumbnail required before publishing.");
        }

        if (app.getScreenshots() == null || app.getScreenshots().isEmpty()) {
            log.error("Screenshots missing");
            throw new BadRequestException("At least one screenshot required.");
        }

        // if (app.getDescription() == null || app.getDescription().isBlank()) {
        // log.error("Description missing");
        // throw new BadRequestException("Description required before publishing.");
        // }

    }

    @Override
    public Page<ApplicationResponse> getPublishedApplications(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Application> applications = applicationRepository.findByStatus(ApplicationStatus.PUBLISHED, pageable);

        return applications.map(applicationMapper::toResponse);
    }
}
