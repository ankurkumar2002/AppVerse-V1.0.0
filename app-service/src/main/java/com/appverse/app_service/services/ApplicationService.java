package com.appverse.app_service.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.appverse.app_service.dto.ApplicationRequest;
import com.appverse.app_service.dto.ApplicationResponse;
import com.appverse.app_service.dto.MessageResponse;
import com.appverse.app_service.dto.ScreenshotRequest;
import com.appverse.app_service.dto.UpdateApplicationRequest;
import com.appverse.app_service.enums.ApplicationStatus;

public interface ApplicationService {

        MessageResponse createApplication(ApplicationRequest request, MultipartFile thumbnail,
                        List<MultipartFile> screenshots, List<ScreenshotRequest> metadata, String developerId);

        MessageResponse updateApplication(String id, UpdateApplicationRequest application,
                        MultipartFile thumbnail,
                        List<MultipartFile> screenshots,
                        List<ScreenshotRequest> metadata, String developerId);

        void deleteApplication(String id, String developerId);

        ApplicationResponse getApplicationById(String id);

        List<ApplicationResponse> getAllApplications();

        MessageResponse updateApplicationStatus(
                        String appId,
                        ApplicationStatus newStatus,
                        String developerId);

        Page<ApplicationResponse> getPublishedApplications(int page, int size);

}
