// === In app-service Project ===
package com.appverse.app_service.services.serviceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
// import java.util.Collections; // Not strictly needed in this version
import java.util.List;
import java.util.UUID;
// import java.util.stream.Collectors; // Not strictly needed in this version

import com.appverse.app_service.enums.MonetizationType;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.appverse.app_service.client.DeveloperClient;
import com.appverse.app_service.client.SubscriptionServiceClient; // <<< CORRECT: Only import the interface

// DO NOT HAVE THESE IMPORTS if the records are nested:
// import com.appverse.app_service.client.SubscriptionServicePlanCreationRequest; // <<< REMOVE THIS
// import com.appverse.app_service.client.SubscriptionServicePlanResponse;    // <<< REMOVE THIS

import com.appverse.app_service.dto.ApplicationRequest;
import com.appverse.app_service.dto.ApplicationResponse;
import com.appverse.app_service.dto.DeveloperOfferedSubscriptionPlanDto;
import com.appverse.app_service.dto.MessageResponse;
import com.appverse.app_service.dto.ScreenshotRequest;
import com.appverse.app_service.dto.UpdateApplicationRequest;
import com.appverse.app_service.exception.BadRequestException;
import com.appverse.app_service.exception.CreationException;
import com.appverse.app_service.exception.DatabaseOperationException;
import com.appverse.app_service.exception.DuplicateKeyException;
import com.appverse.app_service.exception.DuplicateResourceException;
import com.appverse.app_service.exception.ResourceNotFoundException;
import com.appverse.app_service.exception.UpdateOperationException;
import com.appverse.app_service.mapper.ApplicationMapper;
import com.appverse.app_service.model.Application;
import com.appverse.app_service.model.Screenshot;
import com.appverse.app_service.repository.ApplicationRepository;
import com.appverse.app_service.repository.CategoryRepository;
import com.appverse.app_service.services.ApplicationService;
import com.appverse.app_service.services.createService.ApplicationCreateService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationCreateService applicationCreateService;
    private final ApplicationMapper applicationMapper;
    private final CategoryRepository categoryRepository;
    private final DeveloperClient developerClient;
    private final SubscriptionServiceClient subscriptionServiceClient;

    @Override
    @Transactional
    public MessageResponse createApplication(ApplicationRequest request, MultipartFile thumbnail,
            List<MultipartFile> screenshots, List<ScreenshotRequest> metadata) {

        log.info("Attempting to create application with name: {}", request.name());

        // VALIDATION BLOCK 1
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Application name cannot be empty");
        }
        if (applicationRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("An application with this name already exists.");
        }
        categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category ID " + request.categoryId() + " not found."));

        // VALIDATION BLOCK 2 (FEIGN CALL to Developer Service)
        try {
            log.debug("Validating developer ID: {}", request.developerId());
            if (!developerClient.isDeveloperById(request.developerId())) {
                throw new ResourceNotFoundException("Invalid or non-existent developer ID: " + request.developerId());
            }
            log.debug("Developer ID {} validated successfully.", request.developerId());
        } catch (FeignException ex) {
            log.error("FeignException while validating developer ID {}: Status {}, Message: {}", request.developerId(), ex.status(), ex.getMessage(), ex);
            throw new BadRequestException("Failed to validate developer ID. External service may be unavailable or ID is invalid.");
        } catch (Exception e) {
            log.error("Unexpected error while validating developer ID {}: {}", request.developerId(), e.getMessage(), e);
            throw new CreationException("Unexpected error during developer validation.");
        }

        // Validate monetization type with offered plans
        if ((request.monetizationType() == MonetizationType.FREE || request.monetizationType() == MonetizationType.ONE_TIME_PURCHASE) &&
            (request.offeredSubscriptionPlans() != null && !request.offeredSubscriptionPlans().isEmpty())) {
            throw new BadRequestException("Subscription plans cannot be offered for FREE or purely ONE_TIME_PURCHASE applications through this field. Adjust monetizationType.");
        }
        if ((request.monetizationType() == MonetizationType.SUBSCRIPTION_ONLY || request.monetizationType() == MonetizationType.ONE_TIME_OR_SUBSCRIPTION) &&
            (request.offeredSubscriptionPlans() == null || request.offeredSubscriptionPlans().isEmpty())) {
            log.warn("Application monetizationType indicates subscription, but no offeredSubscriptionPlans provided for app: {}", request.name());
        }


        Application application = applicationCreateService.toEntity(request);

        if (application.getMonetizationType() == MonetizationType.FREE) {
            application.setFree(true);
            application.setPrice(BigDecimal.ZERO);
            application.setCurrency(null);
        } else if (application.getMonetizationType() == MonetizationType.SUBSCRIPTION_ONLY) {
            application.setFree(false);
            if (application.getPrice() == null || application.getPrice().compareTo(BigDecimal.ZERO) != 0) {
                 log.warn("For SUBSCRIPTION_ONLY app '{}', price is expected to be 0. Setting it to 0.", application.getName());
                 application.setPrice(BigDecimal.ZERO);
                 application.setCurrency(null);
            }
        } else if (application.getMonetizationType() == MonetizationType.ONE_TIME_PURCHASE || application.getMonetizationType() == MonetizationType.ONE_TIME_OR_SUBSCRIPTION) {
            if (application.getPrice() == null || application.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Price must be provided and non-negative for purchasable monetization types.");
            }
            if (application.getPrice().compareTo(BigDecimal.ZERO) > 0 && (application.getCurrency() == null || application.getCurrency().isBlank())) {
                 throw new BadRequestException("Currency must be provided for priced items.");
            }
            application.setFree(application.getPrice().compareTo(BigDecimal.ZERO) == 0);
        }

        if (thumbnail != null && !thumbnail.isEmpty()) { /* ... file handling ... */ 
            try {
                String filename = UUID.randomUUID().toString() + "_" + Paths.get(thumbnail.getOriginalFilename()).getFileName().toString();
                Path path = Paths.get("uploads/thumbnails/" + filename);
                Files.createDirectories(path.getParent());
                Files.copy(thumbnail.getInputStream(), path);
                application.setThumbnailUrl("/uploads/thumbnails/" + filename);
                log.debug("Thumbnail uploaded to: {}", application.getThumbnailUrl());
            } catch (IOException e) {
                log.error("Failed to upload thumbnail for application {}: {}", request.name(), e.getMessage(), e);
                throw new CreationException("Failed to process thumbnail image."+ e);
            }
        }

        List<Screenshot> screenshotEntities = new ArrayList<>();
        if (screenshots != null && !screenshots.isEmpty()) { /* ... file handling ... */ 
             if (screenshots.size() > 5) {
                throw new BadRequestException("You can upload a maximum of 5 screenshots.");
            }
            for (int i = 0; i < screenshots.size(); i++) {
                MultipartFile screenshotFile = screenshots.get(i);
                if (!screenshotFile.isEmpty()) {
                    try {
                        String filename = UUID.randomUUID().toString() + "_" + Paths.get(screenshotFile.getOriginalFilename()).getFileName().toString();
                        Path path = Paths.get("uploads/screenshots/" + filename);
                        Files.createDirectories(path.getParent());
                        Files.copy(screenshotFile.getInputStream(), path);
                        String screenshotUrl = "/uploads/screenshots/" + filename;

                        ScreenshotRequest meta = (metadata != null && i < metadata.size()) ? metadata.get(i) : null;
                        Screenshot screenshotObj = Screenshot.builder()
                                .id(UUID.randomUUID().toString()) // Screenshot ID
                                .imageUrl(screenshotUrl)
                                .order(meta != null ? meta.order() : i)
                                .caption(meta != null ? meta.caption() : null)
                                .build();
                        screenshotEntities.add(screenshotObj);
                        log.debug("Screenshot {} uploaded to: {}", i + 1, screenshotUrl);
                    } catch (IOException e) {
                        log.error("Failed to upload screenshot #{} for application {}: {}", i + 1, request.name(), e.getMessage(), e);
                        throw new CreationException("Failed to process screenshot image #" + (i + 1)+ e);
                    }
                }
            }
        }
        application.setScreenshots(screenshotEntities);

        Application savedApplication;
        try {
            savedApplication = applicationRepository.save(application);
            log.info("Application {} (ID: {}) saved to database initially.", savedApplication.getName(), savedApplication.getId());
        } catch (DataAccessException e) {
            log.error("Database error while saving application {}: {}", application.getName(), e.getMessage(), e);
            throw new DatabaseOperationException("Failed to save application due to a database issue."+ e);
        }


        List<String> createdPlanIds = new ArrayList<>();
        if (request.offeredSubscriptionPlans() != null && !request.offeredSubscriptionPlans().isEmpty()) {
            log.info("Processing {} offered subscription plans for application ID: {}", request.offeredSubscriptionPlans().size(), savedApplication.getId());
            for (DeveloperOfferedSubscriptionPlanDto planDto : request.offeredSubscriptionPlans()) {
                // Use the Feign client's nested record type
                SubscriptionServiceClient.SubscriptionServicePlanCreationRequest planCreationRequest =
                    new SubscriptionServiceClient.SubscriptionServicePlanCreationRequest(
                        planDto.planNameKey(),
                        planDto.displayName(),
                        planDto.description(),
                        planDto.price(),
                        planDto.currency(),
                        planDto.billingInterval().name(),
                        planDto.billingIntervalCount(),
                        planDto.trialPeriodDays(),
                        savedApplication.getId(),
                        savedApplication.getDeveloperId()
                    );
                try {
                    log.debug("Calling subscription-service to create plan: {}", planDto.displayName());
                    // Use the Feign client's nested record type
                    ResponseEntity<SubscriptionServiceClient.SubscriptionServicePlanResponse> planResponse =
                            subscriptionServiceClient.createDeveloperSubscriptionPlan(planCreationRequest);

                    if (planResponse.getStatusCode().is2xxSuccessful() && planResponse.getBody() != null) {
                        String newPlanId = planResponse.getBody().id();
                        createdPlanIds.add(newPlanId);
                        log.info("Successfully created subscription plan '{}' (ID: {}) for application ID: {}",
                                planDto.displayName(), newPlanId, savedApplication.getId());
                    } else {
                        log.error("Failed to create subscription plan '{}' in subscription-service for app {}. Response status: {}, Body: {}",
                                planDto.displayName(), savedApplication.getId(), planResponse.getStatusCode(), planResponse.getBody());
                        throw new CreationException("Failed to create associated subscription plan: " + planDto.displayName() +
                                                    ". App creation rolled back. Reason: " + planResponse.getStatusCode());
                    }
                } catch (FeignException ex) {
                    log.error("FeignException while creating subscription plan '{}' for app {}: Status {}, Message: {}",
                            planDto.displayName(), savedApplication.getId(), ex.status(), ex.getMessage(), ex);
                    throw new CreationException("Failed to communicate with subscription service for plan: " + planDto.displayName() + ". App creation rolled back."+ ex);
                } catch (Exception ex) {
                     log.error("Unexpected exception while creating subscription plan '{}' for app {}: {}",
                            planDto.displayName(), savedApplication.getId(), ex.getMessage(), ex);
                    throw new CreationException("Unexpected error creating subscription plan: " + planDto.displayName() + ". App creation rolled back."+ ex);
                }
            }

            if (!createdPlanIds.isEmpty()) {
                savedApplication.setApplicationSpecificSubscriptionPlanIds(createdPlanIds);
                try {
                    savedApplication = applicationRepository.save(savedApplication); // Save again
                    log.info("Updated application {} with {} associated subscription plan IDs.", savedApplication.getId(), createdPlanIds.size());
                } catch (DataAccessException e) {
                    log.error("Database error while updating application {} with plan IDs: {}", savedApplication.getId(), e.getMessage(), e);
                    throw new DatabaseOperationException("Failed to link subscription plans to application."+ e);
                }
            }
        }

        log.info("Application processing complete for ID: {}", savedApplication.getId());
        return new MessageResponse("Application created successfully", savedApplication.getId());
    }

    // --- Other methods (updateApplication, deleteApplication, getApplicationById, getAllApplications) ---
    // These methods would also need to be here. I'm omitting them for brevity as the focus is on the createApplication error.
    // Ensure your ApplicationMapper handles mapping the new fields for getApplicationById and getAllApplications.
    // updateApplication would need significant logic to handle changes to offeredSubscriptionPlans.

    @Override
    @Transactional
    public MessageResponse updateApplication(String id, UpdateApplicationRequest request,
            MultipartFile thumbnail,
            List<MultipartFile> screenshots,
            List<ScreenshotRequest> metadata) {
        // ... (Implementation as provided before, but consider how to update offeredSubscriptionPlans) ...
        log.info("Attempting to update application with ID: {}", id);
        Application existingApp = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID " + id + " not found."));

        // Basic validations
        if (request.name() != null && !existingApp.getName().equalsIgnoreCase(request.name())
                && applicationRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("An application with name '" + request.name() + "' already exists.");
        }
        if (request.categoryId() != null) { // Only validate if categoryId is part of the update request
            categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category ID " + request.categoryId() + " not found."));
        }
        applicationMapper.updateFromDto(request, existingApp); // Ensure mapper handles new fields if in UpdateApplicationRequest

        // Consistency logic for monetizationType, price, isFree
        if (existingApp.getMonetizationType() == MonetizationType.FREE) { /* ... */ }
        else if (existingApp.getMonetizationType() == MonetizationType.SUBSCRIPTION_ONLY) { /* ... */ }
        else if (existingApp.getMonetizationType() == MonetizationType.ONE_TIME_PURCHASE || existingApp.getMonetizationType() == MonetizationType.ONE_TIME_OR_SUBSCRIPTION) { /* ... */ }
        
        // File handling for thumbnail and screenshots (similar to create, with deletion of old files)
        // ...

        // TODO: Implement robust logic for updating/adding/deleting associated subscription plans
        // This would involve calling SubscriptionServiceClient
        log.warn("Updating 'offeredSubscriptionPlans' via app update is not fully implemented yet. Only basic app fields are updated.");


        Application updatedApp = applicationRepository.save(existingApp);
        log.info("Application {} updated successfully.", updatedApp.getId());
        return new MessageResponse("Application Updated Successfully!", updatedApp.getId());
    }

    @Override
    @Transactional
    public void deleteApplication(String id) {
        // ... (Implementation as provided before) ...
        log.info("Attempting to delete application with ID: {}", id);
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID " + id + " not found, cannot delete."));
        // TODO: Notify subscription-service to handle/deactivate plans for this app
        applicationRepository.deleteById(id);
        log.info("Application with ID: {} deleted successfully.", id);
        // TODO: Delete files from disk
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(String id) {
        log.debug("Fetching application by ID: {}", id);
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));
        return applicationMapper.toResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications() {
        log.debug("Fetching all applications.");
        List<Application> applications = applicationRepository.findAll();
        return applicationMapper.toResponseList(applications);
    }
}