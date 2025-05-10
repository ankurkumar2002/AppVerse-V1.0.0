package com.appverse.app_service.services;

import java.util.List;

import com.appverse.app_service.model.Screenshot;

public interface ScreenshotService {
    
    Screenshot createScreenshot(Screenshot Screenshot);

    Screenshot updateScreenshot(String id, Screenshot Screenshot);

    void deleteScreenshot(String id);

    Screenshot getScreenshotById(String id);

    List<Screenshot> getAll();
}
