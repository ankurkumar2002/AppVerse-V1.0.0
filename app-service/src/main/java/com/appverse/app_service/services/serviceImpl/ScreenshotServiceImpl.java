package com.appverse.app_service.services.serviceImpl;


import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.appverse.app_service.exception.CreationException;
import com.appverse.app_service.exception.DatabaseOperationException;
import com.appverse.app_service.exception.ResourceNotFoundException;
import com.appverse.app_service.exception.UpdateOperationException;
import com.appverse.app_service.model.Screenshot;
import com.appverse.app_service.repository.ScreenshotRepository;
import com.appverse.app_service.services.ScreenshotService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScreenshotServiceImpl implements ScreenshotService {

    private final ScreenshotRepository screenshotRepository;

    @Override
    @Transactional
    public Screenshot createScreenshot(Screenshot screenshot) {
        try {
            return screenshotRepository.save(screenshot);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Database error occurred while saving screenshot.");
        } catch (Exception e) {
            throw new CreationException("Failed to create screenshot.");
        }
    }

    @Override
    @Transactional
    public Screenshot updateScreenshot(String id, Screenshot screenshot) {
        Screenshot existingScreenshot = screenshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screenshot with ID " + id + " not found."));

        try {
            existingScreenshot.setImageUrl(screenshot.getImageUrl());
            existingScreenshot.setCaption(screenshot.getCaption());

            return screenshotRepository.save(existingScreenshot);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Database error while updating screenshot with ID: " + id);
        } catch (Exception e) {
            throw new UpdateOperationException("Failed to update screenshot with ID: " + id);
        }
    }

    @Override
    @Transactional
    public void deleteScreenshot(String id) {
        if (!screenshotRepository.existsById(id)) {
            throw new ResourceNotFoundException("Screenshot with ID " + id + " not found.");
        }

        try {
            screenshotRepository.deleteById(id);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Failed to delete screenshot with ID: " + id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Screenshot getScreenshotById(String id) {
        return screenshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screenshot with ID " + id + " not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Screenshot> getAll() {
        return screenshotRepository.findAll();
    }
}
