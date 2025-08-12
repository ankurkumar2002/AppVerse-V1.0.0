// === In app-service Project ===
package com.appverse.app_service.services.serviceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import com.appverse.app_service.enums.MonetizationType;
import com.appverse.app_service.event.EventMetaData;
import com.appverse.app_service.event.payload.ApplicationCreatedPayload;
import com.appverse.app_service.event.payload.ApplicationDeletedPayload;
import com.appverse.app_service.event.payload.ApplicationUpdatedPayload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.appverse.app_service.client.DeveloperClient;
import com.appverse.app_service.client.SubscriptionServiceClient;

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

    @Value("${app.upload-dir}")
    private String uploadDir;

    private final ApplicationRepository applicationRepository;
    private final ApplicationCreateService applicationCreateService;
    private final ApplicationMapper applicationMapper;
    private final CategoryRepository categoryRepository;
    private final DeveloperClient developerClient;
    private final SubscriptionServiceClient subscriptionServiceClient;

    private final KafkaTemplate<String, Object> kafkaTemplate; // Inject KafkaTemplate

    private static final String APPLICATION_EVENTS_TOPIC = "application-events";
    private static final String SERVICE_NAME = "app-service";

    private final Executor applicationTaskExecutor;

    @Override
    @Transactional
    public MessageResponse createApplication(ApplicationRequest request, MultipartFile thumbnail,
            List<MultipartFile> screenshots, List<ScreenshotRequest> metadata) {

        log.info("Attempting to create application with name: {}", request.name());

        // --- STEP 1: Perform initial, sequential validations ---
        // These must pass before we do any heavy lifting. The logic is unchanged.
        validateBasicRequest(request);
        validateMonetizationAndPlans(request);

        Application application = applicationCreateService.toEntity(request);
        adjustPricingAndFlags(application);

        // --- STEP 2: Execute independent, long-running tasks in parallel ---
        CompletableFuture<Void> developerValidationFuture = CompletableFuture.runAsync(
                () -> validateDeveloper(request), applicationTaskExecutor);

        CompletableFuture<String> thumbnailUploadFuture = CompletableFuture.supplyAsync(
                () -> handleThumbnailUpload(thumbnail, request.name()), applicationTaskExecutor);

        CompletableFuture<List<Screenshot>> screenshotUploadFuture = CompletableFuture.supplyAsync(
                () -> handleScreenshotUpload(screenshots, metadata, request.name()), applicationTaskExecutor);

        try {
            // --- STEP 3: Wait for all parallel tasks to complete ---
            log.debug("Waiting for parallel validation and upload tasks to complete...");
            CompletableFuture.allOf(developerValidationFuture, thumbnailUploadFuture, screenshotUploadFuture).join();
            log.debug("All parallel tasks completed successfully.");
        } catch (CompletionException e) {
            // Unwrap and re-throw the original exception from the async task
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new CreationException("An unexpected error occurred during a parallel task." + e.getCause());
        }

        // --- STEP 4: Collect results from completed futures and update the entity ---
        String thumbnailUrl = thumbnailUploadFuture.join();
        if (thumbnailUrl != null) {
            application.setThumbnailUrl(thumbnailUrl);
        }

        List<Screenshot> screenshotEntities = screenshotUploadFuture.join();
        application.setScreenshots(screenshotEntities);

        // --- STEP 5: Proceed with the rest of the logic, which is unchanged ---
        Application savedApplication = saveApplication(application);

        List<String> createdPlanIds = handleSubscriptionPlanCreation(request, savedApplication);

        if (!createdPlanIds.isEmpty()) {
            savedApplication.setApplicationSpecificSubscriptionPlanIds(createdPlanIds);
            updateApplicationWithPlanIds(savedApplication);
        }

        log.info("Application processing complete for ID: {}", savedApplication.getId());
        return new MessageResponse("Application created successfully", savedApplication.getId());
    }

    private void validateBasicRequest(ApplicationRequest request) {
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

    private void validateDeveloper(ApplicationRequest request) {
        String keycloakUserId  = request.developerId();
        if (keycloakUserId  == null || keycloakUserId .isBlank()) {
            log.warn("Developer ID is missing or empty. Attempting to use authenticated user context.");
            throw new BadRequestException("Developer ID is required for application creation.");
        }

        try {
            log.debug("Validating developer ID: {}", keycloakUserId );
            if (!developerClient.isDeveloperByKeycloakId(keycloakUserId )) {
                throw new ResourceNotFoundException("Invalid or non-existent developer ID: " + keycloakUserId );
            }
            log.debug("Developer ID {} validated successfully.", keycloakUserId );
        } catch (FeignException ex) {
            log.error("FeignException while validating developer ID {}: Status {}, Message: {}", keycloakUserId ,
                    ex.status(), ex.getMessage(), ex);
            throw new BadRequestException(
                    "Failed to validate developer ID. External service may be unavailable or ID is invalid.");
        } catch (Exception e) {
            log.error("Unexpected error while validating developer ID {}: {}", keycloakUserId , e.getMessage(), e);
            throw new CreationException("Unexpected error during developer validation for ID: " + keycloakUserId );
        }
    }

    private void validateMonetizationAndPlans(ApplicationRequest request) {
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

    private void adjustPricingAndFlags(Application application) {
        switch (application.getMonetizationType()) {
            case FREE -> {
                application.setFree(true);
                application.setPrice(BigDecimal.ZERO);
                application.setCurrency(null);
            }
            case SUBSCRIPTION -> {
                application.setFree(false);
                if (application.getPrice() == null || application.getPrice().compareTo(BigDecimal.ZERO) != 0) {
                    log.warn("For SUBSCRIPTION_ONLY app '{}', price is expected to be 0. Setting it to 0.",
                            application.getName());
                    application.setPrice(BigDecimal.ZERO);
                    application.setCurrency(null);
                }
            }
            case ONE_TIME_PURCHASE, ONE_TIME_OR_SUBSCRIPTION -> {
                if (application.getPrice() == null || application.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new BadRequestException(
                            "Price must be provided and non-negative for purchasable monetization types.");
                }
                if (application.getPrice().compareTo(BigDecimal.ZERO) > 0
                        && (application.getCurrency() == null || application.getCurrency().isBlank())) {
                    throw new BadRequestException("Currency must be provided for priced items.");
                }
                application.setFree(application.getPrice().compareTo(BigDecimal.ZERO) == 0);
            }
        }
    }

    private String handleThumbnailUpload(MultipartFile thumbnail, String applicationName) {
        if (thumbnail == null || thumbnail.isEmpty()) {
            return null; // Return null if there's nothing to upload
        }
        try {
            String newFileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + "_"
                    + Paths.get(thumbnail.getOriginalFilename()).getFileName();
            Path path = Paths.get(uploadDir, "thumbnails", newFileName);
            Files.createDirectories(path.getParent());
            Files.copy(thumbnail.getInputStream(), path);
            String thumbnailUrl = "/uploads/thumbnails/" + newFileName;
            log.debug("Thumbnail uploaded to: {} and URL generated: {}", path.toAbsolutePath(), thumbnailUrl);
            return thumbnailUrl;
        } catch (IOException e) {
            log.error("Failed to upload thumbnail for application {}: {}", applicationName, e.getMessage(), e);
            throw new CreationException("Failed to process thumbnail image." + e);
        }
    }

    private List<Screenshot> handleScreenshotUpload(List<MultipartFile> screenshots, List<ScreenshotRequest> metadata,
            String applicationName) {
        List<Screenshot> screenshotEntities = new ArrayList<>();
        if (screenshots == null || screenshots.isEmpty()) {
            return screenshotEntities; // Return empty list
        }

        for (int i = 0; i < screenshots.size(); i++) {
            MultipartFile screenshotFile = screenshots.get(i);
            if (screenshotFile != null && !screenshotFile.isEmpty()) {
                try {
                    String newFileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + "_"
                            + Paths.get(screenshotFile.getOriginalFilename()).getFileName();
                    Path path = Paths.get(uploadDir, "screenshots", newFileName);
                    Files.createDirectories(path.getParent());
                    Files.copy(screenshotFile.getInputStream(), path);

                    String screenshotUrl = "/uploads/screenshots/" + newFileName;
                    ScreenshotRequest meta = (metadata != null && i < metadata.size()) ? metadata.get(i) : null;

                    Screenshot screenshotObj = Screenshot.builder()
                            .id(UUID.randomUUID().toString())
                            .imageUrl(screenshotUrl)
                            .order(meta != null ? meta.order() : i)
                            .caption(meta != null ? meta.caption() : null)
                            .build();

                    screenshotEntities.add(screenshotObj);
                    log.debug("Screenshot {} uploaded to: {} and DB URL set to: {}", i + 1, path.toAbsolutePath(),
                            screenshotUrl);
                } catch (IOException e) {
                    log.error("Failed to upload screenshot #{} for application {}: {}", i + 1, applicationName,
                            e.getMessage(), e);
                    throw new CreationException("Failed to process screenshot image #" + (i + 1) + e);
                }
            }
        }
        return screenshotEntities;
    }

    private Application saveApplication(Application application) {
        try {
            Application saved = applicationRepository.save(application);
            log.info("Application {} (ID: {}) saved to database initially.", saved.getName(), saved.getId());

            kafkaTemplate.send(APPLICATION_EVENTS_TOPIC, saved.getId(), new ApplicationCreatedPayload(
                    saved.getId(), saved.getName(), saved.getDeveloperId(), saved.getCategoryId(),
                    saved.getMonetizationType(), saved.getPrice(), saved.getCurrency(), saved.isFree(),
                    saved.getPlatforms(), saved.getStatus(), saved.getTags(), saved.getCreatedAt(),
                    saved.getAssociatedSubscriptionPlanIds()));
            log.info("Published ApplicationCreatedEvent for app ID: {}", saved.getId());
            return saved;
        } catch (DataAccessException e) {
            log.error("Database error while saving application {}: {}", application.getName(), e.getMessage(), e);
            throw new DatabaseOperationException("Failed to save application due to a database issue." + e);
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

    private void updateApplicationWithPlanIds(Application savedApplication) {
        try {
            applicationRepository.save(savedApplication);
            log.info("Updated application {} with {} associated subscription plan IDs.",
                    savedApplication.getId(), savedApplication.getApplicationSpecificSubscriptionPlanIds().size());
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to link subscription plans to application." + e);
        }
    }

    @Override
    @Transactional
    public MessageResponse updateApplication(String id, UpdateApplicationRequest request,
            MultipartFile thumbnail,
            List<MultipartFile> screenshots,
            List<ScreenshotRequest> metadata) {

        log.info("Attempting to update application with ID: {}", id);

        // --- STEP 1: Sequential validation and data fetching (Unchanged) ---
        Application existingApp = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID " + id + " not found."));

        validateUpdateRequest(request, existingApp);
        applicationMapper.updateFromDto(request, existingApp);
        // adjustPricingAndFlags(existingApp); // Optional: Add if price/monetization
        // can be updated

        // --- STEP 2: Execute independent, I/O-bound update tasks in parallel ---
        CompletableFuture<String> thumbnailUpdateFuture = CompletableFuture.supplyAsync(
                () -> handleThumbnailUpdate(thumbnail, existingApp.getThumbnailUrl(), existingApp.getName()),
                applicationTaskExecutor);

        // NEW: Handle screenshot updates in parallel
        CompletableFuture<List<Screenshot>> screenshotUpdateFuture = CompletableFuture.supplyAsync(
                () -> handleScreenshotUpdate(screenshots, metadata, existingApp.getScreenshots(),
                        existingApp.getName()),
                applicationTaskExecutor);

        try {
            // --- STEP 3: Wait for ALL parallel tasks to complete ---
            log.debug("Waiting for parallel update tasks to complete for app ID: {}", id);
            CompletableFuture.allOf(thumbnailUpdateFuture, screenshotUpdateFuture).join();
            log.debug("Parallel update tasks completed successfully for app ID: {}", id);
        } catch (CompletionException e) {
            // Unwrap and re-throw the original exception
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new UpdateOperationException("An error occurred during a parallel update task." + e.getCause());
        }

        // --- STEP 4: Collect results and update the main entity ---
        String newThumbnailUrl = thumbnailUpdateFuture.join();
        // A null URL from the helper means no change was requested.
        if (newThumbnailUrl != null) {
            existingApp.setThumbnailUrl(newThumbnailUrl);
        }

        List<Screenshot> newScreenshotEntities = screenshotUpdateFuture.join();
        // A null list from the helper means no change was requested.
        if (newScreenshotEntities != null) {
            // Replace the entire collection of screenshots
            existingApp.getScreenshots().clear();
            existingApp.getScreenshots().addAll(newScreenshotEntities);
        }

        // --- STEP 5: Save the updated entity and publish the event ---
        Application updatedApp = applicationRepository.save(existingApp);
        log.info("Application {} updated successfully in database.", updatedApp.getId());

        publishApplicationUpdatedEvent(updatedApp);

        log.info("Application {} update processing complete.", updatedApp.getId());
        return new MessageResponse("Application Updated Successfully!", updatedApp.getId());
    }

    /**
     * Handles the full lifecycle of updating screenshots for an application.
     * It deletes all old screenshot files and uploads all new ones in parallel.
     *
     * @param newScreenshotFiles The list of new screenshot files from the request.
     * @param newMetadata        The metadata for the new screenshots.
     * @param oldScreenshots     The list of existing Screenshot entities from the
     *                           database.
     * @param appName            The name of the application, for logging.
     * @return A new list of Screenshot entities, or null if no update was
     *         requested.
     */
    private List<Screenshot> handleScreenshotUpdate(List<MultipartFile> newScreenshotFiles,
            List<ScreenshotRequest> newMetadata, List<Screenshot> oldScreenshots, String appName) {

        // If the user did not provide a list of screenshots, they don't intend to
        // update them.
        if (newScreenshotFiles == null) {
            return null;
        }

        List<CompletableFuture<?>> allFutures = new ArrayList<>();

        // --- Phase 1: Schedule deletion of all old screenshot files ---
        if (oldScreenshots != null && !oldScreenshots.isEmpty()) {
            log.debug("Scheduling deletion of {} old screenshots for app '{}'", oldScreenshots.size(), appName);
            for (Screenshot oldScreenshot : oldScreenshots) {
                CompletableFuture<Void> deleteFuture = CompletableFuture.runAsync(() -> {
                    deleteFile(oldScreenshot.getImageUrl());
                }, applicationTaskExecutor);
                allFutures.add(deleteFuture);
            }
        }

        // --- Phase 2: Schedule upload of all new screenshot files ---
        List<CompletableFuture<Screenshot>> uploadFutures = new ArrayList<>();
        if (!newScreenshotFiles.isEmpty()) {
            log.debug("Scheduling upload of {} new screenshots for app '{}'", newScreenshotFiles.size(), appName);
            for (int i = 0; i < newScreenshotFiles.size(); i++) {
                final int index = i;
                MultipartFile screenshotFile = newScreenshotFiles.get(index);

                if (screenshotFile != null && !screenshotFile.isEmpty()) {
                    CompletableFuture<Screenshot> uploadFuture = CompletableFuture.supplyAsync(() -> {
                        try {
                            String newFileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + "_"
                                    + Paths.get(screenshotFile.getOriginalFilename()).getFileName();
                            Path path = Paths.get(uploadDir, "screenshots", newFileName);
                            Files.createDirectories(path.getParent());
                            Files.copy(screenshotFile.getInputStream(), path);

                            String screenshotUrl = "/uploads/screenshots/" + newFileName;
                            ScreenshotRequest meta = (newMetadata != null && index < newMetadata.size())
                                    ? newMetadata.get(index)
                                    : null;

                            return Screenshot.builder()
                                    .id(UUID.randomUUID().toString())
                                    .imageUrl(screenshotUrl)
                                    .order(meta != null ? meta.order() : index)
                                    .caption(meta != null ? meta.caption() : null)
                                    .build();
                        } catch (IOException e) {
                            log.error("Failed to upload new screenshot #{} for app {}: {}", index + 1, appName,
                                    e.getMessage(), e);
                            throw new UpdateOperationException("Failed to process new screenshot #" + (index + 1) + e);
                        }
                    }, applicationTaskExecutor);

                    uploadFutures.add(uploadFuture);
                }
            }
        }

        allFutures.addAll(uploadFutures);

        // --- Phase 3: Wait for all deletion and upload tasks to complete ---
        if (!allFutures.isEmpty()) {
            CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();
        }

        // --- Phase 4: Collect the results from the upload tasks ---
        return uploadFutures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * Helper to safely delete a file based on its relative URL path.
     *
     * @param fileUrl The URL like "/uploads/screenshots/image.jpg"
     */
    private void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        try {
            // Convert URL path back to a system path
            Path path = Paths.get(uploadDir, fileUrl.replace("/uploads/", ""));
            if (Files.exists(path)) {
                Files.delete(path);
                log.debug("Successfully deleted file: {}", path);
            }
        } catch (IOException e) {
            // Log error but don't fail the entire operation, as the main goal is updating.
            log.error("Failed to delete orphaned file [{}]: {}", fileUrl, e.getMessage());
        }
    }

    // A new helper method for validating the update request
    private void validateUpdateRequest(UpdateApplicationRequest request, Application existingApp) {
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

    /**
     * Example helper to handle thumbnail updates.
     * It deletes the old file and uploads the new one.
     * 
     * @return The new file URL, or the old URL if no new file was provided.
     */
    private String handleThumbnailUpdate(MultipartFile newThumbnail, String oldThumbnailUrl, String appName) {
        if (newThumbnail == null || newThumbnail.isEmpty()) {
            return oldThumbnailUrl; // No change
        }

        // 1. Delete the old thumbnail if it exists
        if (oldThumbnailUrl != null && !oldThumbnailUrl.isBlank()) {
            try {
                Path oldPath = Paths.get(uploadDir, oldThumbnailUrl.replace("/uploads/", ""));
                Files.deleteIfExists(oldPath);
                log.debug("Deleted old thumbnail for app {}: {}", appName, oldPath);
            } catch (IOException e) {
                log.error("Failed to delete old thumbnail {} for app {}: {}", oldThumbnailUrl, appName, e.getMessage());
                // Decide if this should be a critical error or just a warning
            }
        }

        // 2. Upload the new thumbnail (reusing existing logic)
        try {
            String newFileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + "_"
                    + Paths.get(newThumbnail.getOriginalFilename()).getFileName();
            Path path = Paths.get(uploadDir, "thumbnails", newFileName);
            Files.createDirectories(path.getParent());
            Files.copy(newThumbnail.getInputStream(), path);
            String newUrl = "/uploads/thumbnails/" + newFileName;
            log.debug("Uploaded new thumbnail for app {}: {}", appName, newUrl);
            return newUrl;
        } catch (IOException e) {
            log.error("Failed to upload new thumbnail for app {}: {}", appName, e.getMessage(), e);
            throw new UpdateOperationException("Failed to process new thumbnail image." + e);
        }
    }

    // Helper to keep the main method cleaner
    private void publishApplicationUpdatedEvent(Application updatedApp) {
        ApplicationUpdatedPayload payload = new ApplicationUpdatedPayload(
                updatedApp.getId(), updatedApp.getName(), updatedApp.getDeveloperId(), updatedApp.getCategoryId(),
                updatedApp.getMonetizationType(), updatedApp.getPrice(), updatedApp.getCurrency(),
                updatedApp.isFree(), updatedApp.getPlatforms(), updatedApp.getStatus(),
                updatedApp.getTags(), updatedApp.getUpdatedAt(), updatedApp.getAssociatedSubscriptionPlanIds());

        kafkaTemplate.send(APPLICATION_EVENTS_TOPIC, updatedApp.getId(), payload);
        log.info("Published ApplicationUpdatedEvent for app ID: {}", updatedApp.getId());
    }

    @Override
    @Transactional
    public void deleteApplication(String id) {
        log.info("Attempting to delete application with ID: {}", id);
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application with ID " + id + " not found, cannot delete."));
        applicationRepository.deleteById(id);
        ApplicationDeletedPayload payload = new ApplicationDeletedPayload(
                application.getId(),
                application.getDeveloperId(),
                application.getName(),
                Instant.now());
        kafkaTemplate.send(APPLICATION_EVENTS_TOPIC, application.getId(), payload);
        log.info("Published ApplicationDeletedEvent for app ID: {}", application.getId());
        log.info("Application with ID: {} deleted successfully.", id);
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