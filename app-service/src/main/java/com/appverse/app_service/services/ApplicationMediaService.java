package com.appverse.app_service.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.appverse.app_service.dto.ScreenshotRequest;
import com.appverse.app_service.model.Screenshot;

public interface ApplicationMediaService {
     String uploadThumbnail(MultipartFile file, String appName);

    String updateThumbnail(
        MultipartFile newThumbnail,
        String oldThumbnailUrl,
        String appName
    );

    List<Screenshot> uploadScreenshots(
        List<MultipartFile> files,
        List<ScreenshotRequest> metadata,
        String appName
    );

    List<Screenshot> updateScreenshots(
        List<MultipartFile> newFiles,
        List<ScreenshotRequest> metadata,
        List<Screenshot> oldScreenshots,
        String appName
    );
}
