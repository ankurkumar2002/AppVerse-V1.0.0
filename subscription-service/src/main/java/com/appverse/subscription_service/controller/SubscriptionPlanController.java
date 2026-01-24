package com.appverse.subscription_service.controller;

import com.appverse.subscription_service.dto.InternalPlanCreationRequest;
import com.appverse.subscription_service.dto.SubscriptionPlanRequest;
import com.appverse.subscription_service.dto.SubscriptionPlanResponse;
import com.appverse.subscription_service.services.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping("/internal")
    public ResponseEntity<SubscriptionPlanResponse> createDeveloperSubscriptionPlan(
            @Valid @RequestBody InternalPlanCreationRequest request) {

        log.info("Creating subscription plan for app={}, developer={}",
                request.applicationId(), request.developerId());

        SubscriptionPlanResponse response =
                subscriptionPlanService.createDeveloperPlan(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{planId}/activate")
    public ResponseEntity<Void> activatePlan(@PathVariable String planId) {
        subscriptionPlanService.activatePlan(planId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{planId}/deactivate")
    public ResponseEntity<Void> deactivatePlan(@PathVariable String planId) {
        subscriptionPlanService.deactivatePlan(planId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<SubscriptionPlanResponse>> getPlansByApplication(
            @PathVariable String applicationId) {

        return ResponseEntity.ok(
                subscriptionPlanService.getPlansByApplicationId(applicationId)
        );
    }
}
