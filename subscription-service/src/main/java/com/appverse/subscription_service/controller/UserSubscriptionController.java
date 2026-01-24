package com.appverse.subscription_service.controller;

import com.appverse.subscription_service.dto.CancelSubscriptionRequest;
import com.appverse.subscription_service.dto.CreateUserSubscriptionRequest;
import com.appverse.subscription_service.dto.UserSubscriptionResponse;
import com.appverse.subscription_service.services.UserSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    @PostMapping("/mine")
    public ResponseEntity<UserSubscriptionResponse> subscribe(
            @Valid @RequestBody CreateUserSubscriptionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("User {} subscribing to plan {}", userId, request.subscriptionPlanId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userSubscriptionService.subscribeUser(userId, request));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<UserSubscriptionResponse>> getMySubscriptions(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                userSubscriptionService.getUserSubscriptions(jwt.getSubject())
        );
    }

    @PostMapping("/mine/{subscriptionId}/cancel")
    public ResponseEntity<UserSubscriptionResponse> cancel(
            @PathVariable String subscriptionId,
            @RequestBody(required = false) CancelSubscriptionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String reason = request != null ? request.reason() : "User cancelled";

        return ResponseEntity.ok(
                userSubscriptionService.cancelUserSubscription(
                        jwt.getSubject(), subscriptionId, reason
                )
        );
    }
}
