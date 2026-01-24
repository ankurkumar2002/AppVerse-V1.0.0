package com.appverse.app_service.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.appverse.app_service.dto.ApplicationRequest;
import com.appverse.app_service.dto.ApplicationResponse;
import com.appverse.app_service.dto.MessageResponse;
import com.appverse.app_service.dto.ScreenshotRequest;
import com.appverse.app_service.dto.UpdateApplicationRequest;


public interface ApplicationService {

    MessageResponse createApplication(ApplicationRequest request, MultipartFile thumbnail, List<MultipartFile> screenshots, List<ScreenshotRequest> metadata, String developerId);

    MessageResponse updateApplication(String id, UpdateApplicationRequest application,
    MultipartFile thumbnail,
    List<MultipartFile> screenshots,
    List<ScreenshotRequest> metadata, String developerId);

    void deleteApplication(String id, String developerId);

    ApplicationResponse getApplicationById(String id);

    List<ApplicationResponse> getAllApplications();
}
