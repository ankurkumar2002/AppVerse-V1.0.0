package com.appverse.app_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.appverse.app_service.model.Category;


public interface CategoryRepository extends MongoRepository<Category, String>{

    boolean existsByNameIgnoreCase(String name);

    boolean existsBySlugIgnoreCase(
            String slug);
    
}
