// === In app-service Project ===
package com.appverse.app_service.services.serviceImpl;

import com.appverse.app_service.dto.CategoryRequest;
import com.appverse.app_service.dto.CategoryResponse;
import com.appverse.app_service.dto.MessageResponse;
import com.appverse.app_service.event.payload.CategoryCreatedPayload; // <<< IMPORT
import com.appverse.app_service.event.payload.CategoryDeletedPayload; // <<< IMPORT
import com.appverse.app_service.event.payload.CategoryUpdatedPayload; // <<< IMPORT
// import com.appverse.app_service.event.EventMetaData; // If using a common event wrapper
import com.appverse.app_service.exception.*; // Assuming all custom exceptions are here
import com.appverse.app_service.mapper.CategoryMapper;
import com.appverse.app_service.model.Category;
import com.appverse.app_service.repository.CategoryRepository;
import com.appverse.app_service.services.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <<< IMPORT FOR LOGGING
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate; // <<< IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant; // <<< IMPORT
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // <<< ADD ANNOTATION FOR LOGGING
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate; // <<< INJECT KAFKA TEMPLATE

    private static final String CATEGORY_EVENTS_TOPIC = "category-events"; // Define Kafka topic
    // private static final String SERVICE_NAME = "app-service"; // If using EventMetaData

    @Override
    @Transactional
    public MessageResponse createCategory(CategoryRequest request) {
        log.info("Attempting to create category with name: '{}' and slug: '{}'", request.name(), request.slug());
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Category name cannot be empty");
        }
        if (request.slug() == null || request.slug().isBlank()) {
            throw new BadRequestException("Category slug cannot be empty");
        }
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Category with this name already exists: " + request.name());
        }
        if (categoryRepository.existsBySlugIgnoreCase(request.slug())) {
            throw new DuplicateResourceException("Category with this slug already exists: " + request.slug());
        }

        try {
            Category category = categoryMapper.toEntity(request);
            // Assuming Category entity has @CreatedDate and @LastModifiedDate handled by auditing
            // If not, set them: category.setCreatedAt(Instant.now()); category.setUpdatedAt(Instant.now());

            Category savedCategory = categoryRepository.save(category);
            log.info("Category '{}' created successfully with ID: {}", savedCategory.getName(), savedCategory.getId());

            // --- Publish CategoryCreatedEvent ---
            CategoryCreatedPayload payload = new CategoryCreatedPayload(
                    savedCategory.getId(),
                    savedCategory.getName(),
                    savedCategory.getSlug()
            );
            // EventMetaData meta = new EventMetaData("CategoryCreated", SERVICE_NAME);
            kafkaTemplate.send(CATEGORY_EVENTS_TOPIC, savedCategory.getId(), payload); // Key by category ID
            log.info("Published CategoryCreatedEvent for category ID: {}", savedCategory.getId());

            return new MessageResponse("Category created successfully", savedCategory.getId());

        } catch (DataAccessException ex) { // More specific catch for DB issues during save
            log.error("Database error while creating category with name '{}': {}", request.name(), ex.getMessage(), ex);
            throw new DatabaseOperationException("Failed to create category due to a database issue."+ ex);
        } catch (RuntimeException ex) { // Catch other runtime issues from mapper or unexpected
            log.error("Unexpected error while creating category with name '{}': {}", request.name(), ex.getMessage(), ex);
            throw new CreationException("Failed to create category: " + ex.getMessage()+ ex);
        }
    }

    @Override
    @Transactional
    public MessageResponse updateCategory(String id, CategoryRequest request) {
        log.info("Attempting to update category with ID: {}", id);
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Check for name conflict only if name is changing
        if (request.name() != null && !request.name().isBlank() &&
            !existingCategory.getName().equalsIgnoreCase(request.name()) &&
            categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Another category with name '" + request.name() + "' already exists.");
        }

        // Check for slug conflict only if slug is changing
        if (request.slug() != null && !request.slug().isBlank() &&
            !existingCategory.getSlug().equalsIgnoreCase(request.slug()) &&
            categoryRepository.existsBySlugIgnoreCase(request.slug())) {
            throw new DuplicateResourceException("Another category with slug '" + request.slug() + "' already exists.");
        }

        try {
            categoryMapper.updateFromDto(request, existingCategory); // This updates fields on existingCategory
            // existingCategory.setUpdatedAt(Instant.now()); // Usually handled by @LastModifiedDate

            Category updatedCategory = categoryRepository.save(existingCategory);
            log.info("Category ID {} updated successfully. New name: '{}'", updatedCategory.getId(), updatedCategory.getName());

            // --- Publish CategoryUpdatedEvent ---
            CategoryUpdatedPayload payload = new CategoryUpdatedPayload(
                    updatedCategory.getId(),
                    updatedCategory.getName(),
                    updatedCategory.getSlug()
            );
            // EventMetaData meta = new EventMetaData("CategoryUpdated", SERVICE_NAME);
            kafkaTemplate.send(CATEGORY_EVENTS_TOPIC, updatedCategory.getId(), payload);
            log.info("Published CategoryUpdatedEvent for category ID: {}", updatedCategory.getId());

            return new MessageResponse("Category updated successfully", updatedCategory.getId());

        } catch (DataAccessException ex) {
            log.error("Database error while updating category ID {}: {}", id, ex.getMessage(), ex);
            throw new DatabaseOperationException("Failed to update category due to a database issue."+ ex);
        } catch (RuntimeException ex) {
            log.error("Unexpected error while updating category ID {}: {}", id, ex.getMessage(), ex);
            throw new UpdateOperationException("Unexpected error updating category: " + ex.getMessage()+ ex);
        }
    }

    @Override
    @Transactional
    public void deleteCategory(String id) {
        log.info("Attempting to delete category with ID: {}", id);
        Category categoryToDelete = categoryRepository.findById(id) // Fetch to get details for event
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id + ", cannot delete."));

        try {
            categoryRepository.deleteById(id);
            log.info("Category ID {} deleted successfully from database.", id);

            // --- Publish CategoryDeletedEvent ---
            CategoryDeletedPayload payload = new CategoryDeletedPayload(
                    categoryToDelete.getId(),
                    categoryToDelete.getName(),
                    categoryToDelete.getSlug(),
                    Instant.now() // Time of deletion event
            );
            // EventMetaData meta = new EventMetaData("CategoryDeleted", SERVICE_NAME);
            kafkaTemplate.send(CATEGORY_EVENTS_TOPIC, categoryToDelete.getId(), payload);
            log.info("Published CategoryDeletedEvent for category ID: {}", categoryToDelete.getId());

        } catch (DataAccessException ex) {
            log.error("Database error while deleting category ID {}: {}", id, ex.getMessage(), ex);
            throw new DatabaseOperationException("Database error while deleting category."+ ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(String id) {
        log.debug("Fetching category by ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        log.debug("Fetching all categories.");
        return categoryMapper.toResponseList(categoryRepository.findAll());
    }
}