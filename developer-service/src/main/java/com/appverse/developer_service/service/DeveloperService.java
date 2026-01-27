package com.appverse.developer_service.service;

import java.nio.file.AccessDeniedException;
import java.util.List;

import com.appverse.developer_service.dto.DeveloperEmailResponse;
import com.appverse.developer_service.dto.DeveloperRequest;
import com.appverse.developer_service.dto.DeveloperResponse;
import com.appverse.developer_service.dto.MessageResponse;

public interface DeveloperService {

    MessageResponse createDeveloper(DeveloperRequest request);

    MessageResponse updateDeveloper(String id, DeveloperRequest request) throws AccessDeniedException;

    void deleteDeveloper(String id) throws AccessDeniedException;

    DeveloperResponse getMyDeveloper();

    boolean existsByKeycloakUserId(String keycloakUserId);
    
    DeveloperEmailResponse getDeveloperEmail(String developerId);

}
