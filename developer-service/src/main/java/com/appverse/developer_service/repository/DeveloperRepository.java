package com.appverse.developer_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.appverse.developer_service.model.Developer;


public interface DeveloperRepository extends JpaRepository<Developer, String>{

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByKeycloakUserId(String keycloakUserId);

    Optional<Developer> findById(String id);

    boolean existsById(String id);

    void deleteById(String id);
    
}
