package com.appverse.developer_service.service;

import java.util.List;

import com.appverse.developer_service.dto.DeveloperRequest;
import com.appverse.developer_service.dto.DeveloperResponse;
import com.appverse.developer_service.dto.MessageResponse;



public interface DeveloperService {

    MessageResponse createDeveloper(DeveloperRequest request);

    MessageResponse updateDeveloper(String id, DeveloperRequest request);

    void deleteDeveloper(String id);

    DeveloperResponse getDeveloperById(String id);

    List<DeveloperResponse> getAll();

    boolean existsById(String id);
}
