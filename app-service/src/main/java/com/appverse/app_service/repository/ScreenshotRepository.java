package com.appverse.app_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.appverse.app_service.model.Screenshot;

public interface ScreenshotRepository extends MongoRepository<Screenshot,String>{
    
}
