package com.appverse.developer_service.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
// Import SecurityContextHolder or Principal if getting user ID from security context
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.appverse.developer_service.dto.DeveloperRequest;
import com.appverse.developer_service.dto.DeveloperResponse;
import com.appverse.developer_service.dto.MessageResponse;
import com.appverse.developer_service.exception.DatabaseOperationException;
import com.appverse.developer_service.exception.DuplicateResourceException;
import com.appverse.developer_service.exception.ResourceNotFoundException;
import com.appverse.developer_service.mapper.DeveloperMapper;
import com.appverse.developer_service.model.Developer;
import com.appverse.developer_service.repository.DeveloperRepository;
import com.appverse.developer_service.service.DeveloperService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeveloperServiceImpl implements DeveloperService {

    private static final Logger log = LoggerFactory.getLogger(DeveloperServiceImpl.class);

    private final DeveloperRepository developerRepository;
    private final DeveloperMapper developerMapper;

    @Override
    @Transactional
    public MessageResponse createDeveloper(DeveloperRequest request) {

        // --- Validation ---

        String keycloakUserId = getKeycloakUserIdFromSecurityContext();
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
             throw new IllegalArgumentException("Keycloak User ID must be provided for developer creation.");
        }
        if (developerRepository.existsByKeycloakUserId(keycloakUserId)) {
             throw new DuplicateResourceException("Developer profile already exists for this user.");
        }
        if (developerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Developer with email '" + request.email() + "' already exists.");
        }
        

        // --- Mapping & Enrichment ---
        Developer developer = developerMapper.toEntity(request);
        developer.setKeycloakUserId(keycloakUserId); // *** SET KEYCLOAK USER ID ***

        // Explicitly set initial status/verification if not relying solely on @Builder.Default
        // developer.setStatus(DeveloperStatus.PENDING_VERIFICATION);
        // developer.setVerified(false);
        // developerType comes from request via mapper

        // Timestamps should be handled by auditing - no manual setting needed

        // --- Persistence ---
        try {
            Developer savedDeveloper = developerRepository.save(developer);
            log.info("Developer created successfully with ID: {} for Keycloak ID: {}", savedDeveloper.getId(), keycloakUserId);
            // Convert Long ID to String for current MessageResponse structure
            return new MessageResponse("Developer created successfully.", savedDeveloper.getId());
        } catch (DataAccessException e) {
            log.error("Database error creating developer for Keycloak ID {}: {}", keycloakUserId, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to create developer due to a database issue.");
        }
    }

    @Override
    @Transactional
    // *** ID parameter changed to Long ***
    public MessageResponse updateDeveloper(String id, DeveloperRequest request) {
        Developer existing = developerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with ID: " + id));

        // Optional: Check email conflict only if email is actually changing
        if (!existing.getEmail().equalsIgnoreCase(request.email()) &&
            developerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Cannot update: Another developer with email '" + request.email() + "' already exists.");
        }

        // --- Mapping ---
        // Use mapper to update fields (handles nulls based on mapper config)
        // It correctly WON'T update id, keycloakUserId, createdAt, status, isVerified etc.
        developerMapper.updateFromDto(request, existing);

        // Timestamp (updatedAt) should be handled by auditing

        // --- Persistence ---
        try {
            Developer updatedDeveloper = developerRepository.save(existing);
            log.info("Developer updated successfully with ID: {}", id);
            // Convert Long ID to String for current MessageResponse structure
            return new MessageResponse("Developer updated successfully.", updatedDeveloper.getId());
        } catch (DataAccessException e) {
            log.error("Database error updating developer with ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to update developer due to a database issue.");
        }
    }

    @Override
    @Transactional
    public void deleteDeveloper(String id) {
        if (!developerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Developer not found with ID: " + id + ", cannot delete.");
        }
        try {
            // Consider finding by ID first if you need to perform actions based on the developer's state before deletion
            developerRepository.deleteById(id);
            log.info("Developer deleted successfully with ID: {}", id);
        } catch (DataAccessException e) {
            log.error("Database error deleting developer with ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to delete developer due to a database issue.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    // *** ID parameter changed to Long ***
    public DeveloperResponse getDeveloperById(String id) {
        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with ID: " + id));
        return developerMapper.toResponse(developer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeveloperResponse> getAll() {
        try {
            List<Developer> developers = developerRepository.findAll();
            // Use correct mapper method reference
            return developerMapper.toResponseList(developers);
        } catch (DataAccessException e) {
             log.error("Database error retrieving all developers: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Failed to retrieve developers due to a database issue.");
        }
    }

     // *** Add existsByKeycloakUserId method signature to DeveloperRepository ***
     // Example: boolean existsByKeycloakUserId(String keycloakUserId);

     @Override
    @Transactional(readOnly = true)
    public boolean existsById(String id) {
        return developerRepository.existsById(id);
    }

    private String getKeycloakUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            // Keycloak User ID is often stored in the 'sub' claim
            return jwt.getClaimAsString("sub"); // The 'sub' claim usually represents the user ID in Keycloak
        }
        return null;
    }

}