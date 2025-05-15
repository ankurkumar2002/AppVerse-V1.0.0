// === In Subscription Service Project ===
package com.appverse.subscription_service.controller;

import com.appverse.subscription_service.dto.InternalPlanCreationRequest;
import com.appverse.subscription_service.dto.SubscriptionPlanRequest;
import com.appverse.subscription_service.dto.SubscriptionPlanResponse;
import com.appverse.subscription_service.services.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For role-based security
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/subscription-plans") // Admin-specific path
@RequiredArgsConstructor
@Slf4j
// @PreAuthorize("hasRole('ADMIN')") // Secure all methods in this controller for ADMIN role
public class SubscriptionPlanController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionPlanResponse> createPlan(@Valid @RequestBody SubscriptionPlanRequest request) {
        log.info("Admin request to create subscription plan: {}", request.name());
        SubscriptionPlanResponse createdPlan = subscriptionService.createPlan(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPlan.id())
                .toUri();
        log.info("Subscription plan {} created with ID: {}", createdPlan.name(), createdPlan.id());
        return ResponseEntity.created(location).body(createdPlan);
    }

    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionPlanResponse> updatePlan(
            @PathVariable String planId,
            @Valid @RequestBody SubscriptionPlanRequest request) {
        log.info("Admin request to update subscription plan ID: {}", planId);
        SubscriptionPlanResponse updatedPlan = subscriptionService.updatePlan(planId, request);
        log.info("Subscription plan {} updated.", planId);
        return ResponseEntity.ok(updatedPlan);
    }

    @GetMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()") // Allow authenticated users to view plan details
    public ResponseEntity<SubscriptionPlanResponse> getPlanById(@PathVariable String planId) {
        log.info("Request to get subscription plan by ID: {}", planId);
        SubscriptionPlanResponse plan = subscriptionService.getPlanById(planId);
        return ResponseEntity.ok(plan);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()") // Allow authenticated users to list plans
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllPlans(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        log.info("Request to get all subscription plans. Active only: {}", activeOnly);
        List<SubscriptionPlanResponse> plans = subscriptionService.getAllPlans(activeOnly);
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/{planId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activatePlan(@PathVariable String planId) {
        log.info("Admin request to activate subscription plan ID: {}", planId);
        subscriptionService.activatePlan(planId);
        log.info("Subscription plan {} activated.", planId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{planId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivatePlan(@PathVariable String planId) {
        log.info("Admin request to deactivate subscription plan ID: {}", planId);
        subscriptionService.deactivatePlan(planId);
        log.info("Subscription plan {} deactivated.", planId);
        return ResponseEntity.noContent().build();
    }

     @PostMapping("/subscription-plans/by-developer")
    // Secure this endpoint appropriately (e.g., service-to-service auth, specific scope/role)
    // @PreAuthorize("hasAuthority('SCOPE_INTERNAL_SERVICE_CALL')")
    public ResponseEntity<SubscriptionPlanResponse> createDeveloperSubscriptionPlan(
            @Valid @RequestBody InternalPlanCreationRequest request) { // Use a specific DTO
        log.info("Internal request to create developer subscription plan for app: {}, dev: {}, nameKey: {}",
                request.applicationId(), request.developerId(), request.planNameKey());
        SubscriptionPlanResponse createdPlan = subscriptionService.createDeveloperPlan(request); // New service method
        // No location header needed for internal call usually, or adjust as needed
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan);
    }
}