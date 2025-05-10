package com.appverse.app_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.appverse.app_service.model.Application;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {

    boolean existsByNameIgnoreCase(String name);
    
}
