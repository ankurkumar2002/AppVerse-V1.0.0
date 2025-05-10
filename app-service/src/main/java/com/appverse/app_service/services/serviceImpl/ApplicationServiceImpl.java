package com.appverse.app_service.services.serviceImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.appverse.app_service.client.DeveloperClient;
import com.appverse.app_service.dto.ApplicationRequest;
import com.appverse.app_service.dto.ApplicationResponse;
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

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationCreateService applicationCreateService;
    private final ApplicationMapper applicationMapper;
    private final CategoryRepository categoryRepository;

    private final DeveloperClient developerClient;

    @Override
    @Transactional
    public MessageResponse createApplication(ApplicationRequest request, MultipartFile thumbnail,
            List<MultipartFile> screenshots, List<ScreenshotRequest> metadata) {

        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Application name cannot be empty");
        }

        if (applicationRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("An application with this name already exists.");
        }

        categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category ID " + request.categoryId()));

        try {
            if (!developerClient.isDeveloperById(request.developerId())) {
                throw new ResourceNotFoundException("Invalid developer ID");
            }
        } catch (FeignException ex) {
            ex.printStackTrace();
            throw new BadRequestException("Failed to validate developer ID");
        }

        try {

            if (screenshots != null && screenshots.size() > 5) {
                throw new BadRequestException("You can upload a maximum of 5 screenshots.");
            }
            Application application = applicationCreateService.toEntity(request);
            application.setCreatedAt(Instant.now());
            application.setUpdatedAt(Instant.now());

            String thumbnailUrl = null;
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String filename = UUID.randomUUID() + "_"
                        + Paths.get(thumbnail.getOriginalFilename()).getFileName().toString();

                Path path = Paths.get("uploads/thumbnails/" + filename);
                Files.createDirectories(path.getParent());
                Files.copy(thumbnail.getInputStream(), path);
                thumbnailUrl = "/uploads/thumbnails/" + filename;
                application.setThumbnailUrl(thumbnailUrl);
            }

            List<Screenshot> screenshotEntities = new ArrayList<>();
            if (screenshots != null && !screenshots.isEmpty()) {
                for (int i = 0; i < screenshots.size(); i++) {
                    MultipartFile screenshotFile = screenshots.get(i);
                    if (!screenshotFile.isEmpty()) {
                        String filename = UUID.randomUUID() + "_"
                                + Paths.get(screenshotFile.getOriginalFilename()).getFileName().toString();

                        Path path = Paths.get("uploads/screenshots/" + filename);
                        Files.createDirectories(path.getParent());
                        Files.copy(screenshotFile.getInputStream(), path);
                        String screenshotUrl = "/uploads/screenshots/" + filename;

                        ScreenshotRequest meta = (metadata != null && i < metadata.size()) ? metadata.get(i) : null;

                        Screenshot screenshotObj = Screenshot.builder()
                                .id(UUID.randomUUID().toString())
                                .imageUrl(screenshotUrl)
                                .order(meta != null ? meta.order() : i)
                                .caption(meta != null ? meta.caption() : null)
                                .build();

                        screenshotEntities.add(screenshotObj);
                    }
                }
            }

            application.setScreenshots(screenshotEntities);

            Application saved = applicationRepository.save(application);
            return new MessageResponse("Application created successfully", saved.getId());

        } catch (DuplicateKeyException ex) {
            throw new DuplicateResourceException("A resource with this name already exists.");
        } catch (Exception ex) {
            throw new CreationException("Failed to create application");
        }
    }

    @Override
    @Transactional
    public MessageResponse updateApplication(String id, UpdateApplicationRequest request,
            MultipartFile thumbnail,
            List<MultipartFile> screenshots,
            List<ScreenshotRequest> metadata) {
        Application existingApp = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID " + id + " not found."));

        if (!existingApp.getName().equalsIgnoreCase(request.name())
                && applicationRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("An application with name '" + request.name() + "' already exists.");
        }

        categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category ID " + request.categoryId()));

        // developerRepository.findById(request.developerId())
        // .orElseThrow(() -> new ResourceNotFoundException("Developer ID " +
        // request.developerId()));

        applicationMapper.updateFromDto(request, existingApp);
        existingApp.setUpdatedAt(Instant.now());

        try {
            Application existingApp1 = applicationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Application with ID " + id + " not found."));

            int existingCount = existingApp1.getScreenshots() != null ? existingApp1.getScreenshots().size() : 0;

            int newCount = (screenshots != null) ? screenshots.size() : 0;

            if ((existingCount + newCount) > 5) {
                throw new BadRequestException("You can only have a maximum of 5 screenshots. Currently uploaded: "
                        + existingCount + ", trying to add: " + newCount);
            }

            if (thumbnail != null && !thumbnail.isEmpty()) {

                String oldThumbnailUrl = existingApp.getThumbnailUrl();

                String filename = UUID.randomUUID() + "_"
                        + Paths.get(thumbnail.getOriginalFilename()).getFileName().toString();

                Path path = Paths.get("uploads/thumbnails/" + filename);
                Files.createDirectories(path.getParent());
                Files.copy(thumbnail.getInputStream(), path);
                String thumbnailUrl = "/uploads/thumbnails/" + filename;
                existingApp.setThumbnailUrl(thumbnailUrl);
                Path filePath = Paths.get(oldThumbnailUrl);
                Files.deleteIfExists(filePath);
            }

            List<Screenshot> currentScreenshots = existingApp.getScreenshots() != null
                    ? new ArrayList<>(existingApp.getScreenshots())
                    : new ArrayList<>();

            if (screenshots != null && !screenshots.isEmpty()) {
                for (int i = 0; i < screenshots.size(); i++) {
                    MultipartFile screenshotFile = screenshots.get(i);
                    if (!screenshotFile.isEmpty()) {
                        String filename = UUID.randomUUID() + "_"
                                + Paths.get(screenshotFile.getOriginalFilename()).getFileName().toString();

                        Path path = Paths.get("uploads/screenshots/" + filename);
                        Files.createDirectories(path.getParent());
                        Files.copy(screenshotFile.getInputStream(), path);
                        String screenshotUrl = "/uploads/screenshots/" + filename;

                        ScreenshotRequest meta = (metadata != null && i < metadata.size()) ? metadata.get(i) : null;

                        Screenshot screenshotObj = Screenshot.builder()
                                .id(UUID.randomUUID().toString())
                                .imageUrl(screenshotUrl)
                                .order(meta != null ? meta.order() : i)
                                .caption(meta != null ? meta.caption() : null)
                                .build();

                        currentScreenshots.add(screenshotObj);
                    }
                }
            }

            existingApp.setScreenshots(currentScreenshots);
            Application updated = applicationRepository.save(existingApp);
            return new MessageResponse("Application Updated Successfully!", updated.getId());

        } catch (DuplicateKeyException e) {
            throw new DuplicateResourceException("Database error during update: Name '" + request.name()
                    + "' likely conflicts with an existing application.");
        } catch (DataAccessException e) {
            throw new DatabaseOperationException(
                    "Database error prevented updating application with ID: " + id);
        } catch (Exception e) {
            throw new UpdateOperationException(
                    "An unexpected error occurred while saving the update for application ID: " + id);
        }
    }

    @Override
    @Transactional
    public void deleteApplication(String id) {
        if (!applicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Application with ID " + id + " not found, cannot delete.");
        }
        try {
            applicationRepository.deleteById(id);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException(
                    "Database error occurred while trying to delete application with ID: " + id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(String id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        return applicationMapper.toResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications() {
        List<Application> applications = applicationRepository.findAll();
        return applicationMapper.toResponseList(applications);
    }

}
