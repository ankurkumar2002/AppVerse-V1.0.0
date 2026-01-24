package com.appverse.app_service.services.mediaService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.appverse.app_service.dto.ScreenshotRequest;
import com.appverse.app_service.exception.CreationException;
import com.appverse.app_service.exception.UpdateOperationException;
import com.appverse.app_service.model.Screenshot;
import com.appverse.app_service.services.ApplicationMediaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationMediaServiceImpl implements ApplicationMediaService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    private final Executor applicationTaskExecutor;

    public String uploadThumbnail(MultipartFile thumbnail, String applicationName) {
        if (thumbnail == null || thumbnail.isEmpty()) {
            return null;
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

    public List<Screenshot> updateScreenshots(List<MultipartFile> newScreenshotFiles,
            List<ScreenshotRequest> newMetadata, List<Screenshot> oldScreenshots, String appName) {

        if (newScreenshotFiles == null) {
            return null;
        }

        List<CompletableFuture<?>> allFutures = new ArrayList<>();

        if (oldScreenshots != null && !oldScreenshots.isEmpty()) {
            log.debug("Scheduling deletion of {} old screenshots for app '{}'", oldScreenshots.size(), appName);
            for (Screenshot oldScreenshot : oldScreenshots) {
                CompletableFuture<Void> deleteFuture = CompletableFuture.runAsync(() -> {
                    deleteFile(oldScreenshot.getImageUrl());
                }, applicationTaskExecutor);
                allFutures.add(deleteFuture);
            }
        }

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

        if (!allFutures.isEmpty()) {
            CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();
        }

        return uploadFutures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        try {

            Path path = Paths.get(uploadDir, fileUrl.replace("/uploads/", ""));
            if (Files.exists(path)) {
                Files.delete(path);
                log.debug("Successfully deleted file: {}", path);
            }
        } catch (IOException e) {

            log.error("Failed to delete orphaned file [{}]: {}", fileUrl, e.getMessage());
        }
    }

    public String updateThumbnail(MultipartFile newThumbnail, String oldThumbnailUrl, String appName) {
        if (newThumbnail == null || newThumbnail.isEmpty()) {
            return oldThumbnailUrl;
        }

        if (oldThumbnailUrl != null && !oldThumbnailUrl.isBlank()) {
            try {
                Path oldPath = Paths.get(uploadDir, oldThumbnailUrl.replace("/uploads/", ""));
                Files.deleteIfExists(oldPath);
                log.debug("Deleted old thumbnail for app {}: {}", appName, oldPath);
            } catch (IOException e) {
                log.error("Failed to delete old thumbnail {} for app {}: {}", oldThumbnailUrl, appName, e.getMessage());

            }
        }

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

    public List<Screenshot> uploadScreenshots(List<MultipartFile> screenshots, List<ScreenshotRequest> metadata,
            String applicationName) {
        List<Screenshot> screenshotEntities = new ArrayList<>();
        if (screenshots == null || screenshots.isEmpty()) {
            return screenshotEntities;
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

}
