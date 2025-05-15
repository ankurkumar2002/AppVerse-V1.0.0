// === In Subscription Service Project ===
package com.appverse.subscription_service.controller;

import com.appverse.subscription_service.dto.CancelSubscriptionRequest;
import com.appverse.subscription_service.dto.CreateUserSubscriptionRequest;
import com.appverse.subscription_service.dto.UserSubscriptionResponse;
import com.appverse.subscription_service.services.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions") // Path centered around user's subscriptions
@RequiredArgsConstructor
@Slf4j
// @PreAuthorize("isAuthenticated()") // Secure all methods for authenticated users
public class UserSubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Creates/Initiates a new subscription for the authenticated user.
     */
    @PostMapping("/mine") // Endpoint to create a subscription for "me" (the authenticated user)
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSubscriptionResponse> createUserSubscription(
            @Valid @RequestBody CreateUserSubscriptionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("User {} initiating subscription to plan ID: {}", userId, request.subscriptionPlanId());
        UserSubscriptionResponse createdSubscription = subscriptionService.createUserSubscription(userId, request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath() // Use fromCurrentContextPath if base path is /api/v1/users/subscriptions
                .path("/mine/{id}")       // Path to get this specific subscription
                .buildAndExpand(createdSubscription.id())
                .toUri();
        log.info("Subscription {} created for user {}. Status: {}",
                createdSubscription.id(), userId, createdSubscription.status());
        // Response might be 201 Created if successful immediately,
        // or 202 Accepted if payment requires further user action (e.g., redirect to 3DS)
        // The DTO might need to include info for next steps if not immediately active.
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(createdSubscription);
    }

    /**
     * Retrieves a specific subscription for the authenticated user by its ID.
     */
    @GetMapping("/mine/{subscriptionId}")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSubscriptionResponse> getMySubscriptionById(
            @PathVariable String subscriptionId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("User {} requesting their subscription ID: {}", userId, subscriptionId);
        UserSubscriptionResponse subscription = subscriptionService.getUserSubscriptionById(userId, subscriptionId);
        return ResponseEntity.ok(subscription);
    }

    /**
     * Retrieves all subscriptions for the authenticated user.
     */
    @GetMapping("/mine")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserSubscriptionResponse>> getAllMySubscriptions(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("User {} requesting all their subscriptions.", userId);
        List<UserSubscriptionResponse> subscriptions = subscriptionService.getAllUserSubscriptions(userId);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Cancels an active subscription for the authenticated user.
     * Cancellation is typically effective at the end of the current billing period.
     */
    @PostMapping("/mine/{subscriptionId}/cancel")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSubscriptionResponse> cancelMySubscription(
            @PathVariable String subscriptionId,
            @RequestBody(required = false) CancelSubscriptionRequest cancelRequest, // Reason is optional
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String reason = (cancelRequest != null && cancelRequest.reason() != null) ? cancelRequest.reason() : "User requested cancellation.";
        log.info("User {} requesting to cancel subscription ID: {} with reason: {}", userId, subscriptionId, reason);
        UserSubscriptionResponse cancelledSubscription = subscriptionService.cancelUserSubscription(userId, subscriptionId, reason);
        log.info("Subscription {} for user {} marked as CANCELLED. Will end on: {}",
                subscriptionId, userId, cancelledSubscription.currentPeriodEndDate());
        return ResponseEntity.ok(cancelledSubscription);
    }

    /**
     * Reactivates a previously user-cancelled (but not yet fully expired) subscription.
     */
    @PostMapping("/mine/{subscriptionId}/reactivate")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSubscriptionResponse> reactivateMySubscription(
            @PathVariable String subscriptionId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("User {} requesting to reactivate subscription ID: {}", userId, subscriptionId);
        UserSubscriptionResponse reactivatedSubscription = subscriptionService.reactivateUserSubscription(userId, subscriptionId);
        log.info("Subscription {} for user {} reactivated.", subscriptionId, userId);
        return ResponseEntity.ok(reactivatedSubscription);
    }

    // TODO:
    // - Endpoint for user to update their payment method for a subscription.
    // - Endpoint for user to change their plan (upgrade/downgrade).
}