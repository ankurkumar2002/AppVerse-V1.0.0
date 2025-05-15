// === In Subscription Service Project ===
package com.appverse.subscription_service.services.serviceImpl;

import com.appverse.subscription_service.client.PaymentServiceClient;
import com.appverse.subscription_service.dto.*;
import com.appverse.subscription_service.enums.*; // Import all your enums
import com.appverse.subscription_service.exception.*; // Your custom exceptions
import com.appverse.subscription_service.mapper.SubscriptionMapper;
import com.appverse.subscription_service.model.SubscriptionPlan;
import com.appverse.subscription_service.model.UserSubscription;
import com.appverse.subscription_service.repository.SubscriptionPlanRepository;
import com.appverse.subscription_service.repository.UserSubscriptionRepository;
import com.appverse.subscription_service.services.SubscriptionService;

import feign.FeignException; // Added for catching Feign specific errors
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity; // Added for Feign response
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.scheduling.annotation.Scheduled; // For renewal job

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map; // Added for PaymentServiceClient.InitiatePaymentPayload
import java.util.Optional;
import java.util.UUID; // Added for mocking or if needed
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final SubscriptionMapper subscriptionMapper;
    // private final SubscriptionEventService eventService; // If you implement event auditing

    // --- Plan Management ---
    @Override
    @Transactional
    public SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request) {
        log.info("Admin creating new subscription plan: {}", request.name());
        if (planRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Subscription plan with name '" + request.name() + "' already exists.");
        }
        SubscriptionPlan plan = subscriptionMapper.toSubscriptionPlan(request);
        plan.setStatus(SubscriptionPlanStatus.ACTIVE); // Default new plans to active
        SubscriptionPlan savedPlan = planRepository.save(plan);
        log.info("SubscriptionPlan {} created with ID: {}", savedPlan.getName(), savedPlan.getId());
        return subscriptionMapper.toSubscriptionPlanResponse(savedPlan);
    }

    @Override
    @Transactional
    public SubscriptionPlanResponse updatePlan(String planId, SubscriptionPlanRequest request) {
        log.info("Admin updating subscription plan ID: {}", planId);
        SubscriptionPlan existingPlan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan not found with ID: " + planId));

        if (!existingPlan.getName().equals(request.name()) && planRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Subscription plan with name '" + request.name() + "' already exists.");
        }
        subscriptionMapper.updateSubscriptionPlanFromRequest(request, existingPlan);
        SubscriptionPlan updatedPlan = planRepository.save(existingPlan);
        log.info("SubscriptionPlan {} updated.", updatedPlan.getId());
        return subscriptionMapper.toSubscriptionPlanResponse(updatedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlanById(String planId) {
        log.debug("Fetching plan by ID: {}", planId);
        return planRepository.findById(planId)
                .map(subscriptionMapper::toSubscriptionPlanResponse)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan not found with ID: " + planId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllPlans(boolean activeOnly) {
        log.debug("Fetching all plans. Active only: {}", activeOnly);
        List<SubscriptionPlan> plans;
        if (activeOnly) {
            plans = planRepository.findByStatus(SubscriptionPlanStatus.ACTIVE);
        } else {
            plans = planRepository.findAll();
        }
        return plans.stream()
                .map(subscriptionMapper::toSubscriptionPlanResponse)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void deactivatePlan(String planId) {
        log.info("Admin deactivating plan ID: {}", planId);
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan not found with ID: " + planId));
        plan.setStatus(SubscriptionPlanStatus.INACTIVE);
        planRepository.save(plan);
        log.info("SubscriptionPlan {} deactivated.", planId);
    }

    @Override
    @Transactional
    public void activatePlan(String planId) {
        log.info("Admin activating plan ID: {}", planId);
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan not found with ID: " + planId));
        plan.setStatus(SubscriptionPlanStatus.ACTIVE);
        planRepository.save(plan);
        log.info("SubscriptionPlan {} activated.", planId);
    }

    @Override
    @Transactional
    public SubscriptionPlanResponse createDeveloperPlan(InternalPlanCreationRequest request) {
        log.info("Creating developer plan '{}' for app: {}, dev: {}", request.displayName(), request.applicationId(), request.developerId());
        if (request.applicationId() == null || request.developerId() == null) {
            throw new IllegalArgumentException("Application ID and Developer ID are required for developer plans.");
        }

        SubscriptionPlanBillingInterval interval;
        try {
            interval = SubscriptionPlanBillingInterval.valueOf(request.billingInterval().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid billing interval string: {}", request.billingInterval(), e);
            throw new BadRequestException("Invalid billing interval: " + request.billingInterval());
        }

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.displayName())
                .description(request.description())
                .price(request.price())
                .currency(request.currency())
                .billingInterval(interval)
                .billingIntervalCount(request.billingIntervalCount())
                .trialPeriodDays(request.trialPeriodDays() != null ? request.trialPeriodDays() : 0)
                .applicationId(request.applicationId())
                .developerId(request.developerId())
                .status(SubscriptionPlanStatus.ACTIVE)
                .build();

        SubscriptionPlan savedPlan = planRepository.save(plan);
        log.info("Developer-defined SubscriptionPlan '{}' created with ID {} for app {} by dev {}",
                 savedPlan.getName(), savedPlan.getId(), savedPlan.getApplicationId(), savedPlan.getDeveloperId());
        return subscriptionMapper.toSubscriptionPlanResponse(savedPlan);
    }


    // --- User Subscription Management ---
    @Override
    @Transactional
    public UserSubscriptionResponse createUserSubscription(String userId, CreateUserSubscriptionRequest request) {
        log.info("User {} attempting to subscribe to plan ID: {}", userId, request.subscriptionPlanId());

        SubscriptionPlan plan = planRepository.findByIdAndStatus(request.subscriptionPlanId(), SubscriptionPlanStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active SubscriptionPlan not found or invalid: " + request.subscriptionPlanId()));
        log.info("Fetched active plan successfully: Plan ID {}, Name: {}, Trial Days: {}, Billing Interval: {}",
            plan.getId(), plan.getName(), plan.getTrialPeriodDays(), plan.getBillingInterval());

        Optional<UserSubscription> existingActiveSub = userSubscriptionRepository
            .findByUserIdAndSubscriptionPlanIdAndStatusIn(userId, plan.getId(),
                List.of(UserSubscriptionStatus.ACTIVE, UserSubscriptionStatus.TRIALING, UserSubscriptionStatus.PAST_DUE, UserSubscriptionStatus.PENDING_INITIAL_PAYMENT, UserSubscriptionStatus.INCOMPLETE));
        log.info("Checked for existing conflicting subscription for user {} and plan {}. Present: {}", userId, plan.getId(), existingActiveSub.isPresent());

        if (existingActiveSub.isPresent()) {
            log.warn("User {} already has an active, trialing, pending, or past_due subscription (ID: {}) to plan {}.",
                     userId, existingActiveSub.get().getId(), plan.getId());
            throw new SubscriptionActionNotAllowedException("User already has an active, trialing, pending, or past_due subscription to this plan.");
        }

        Instant now = Instant.now();
        UserSubscription newSubscription = UserSubscription.builder()
                .userId(userId)
                .subscriptionPlanId(plan.getId())
                .startDate(now)
                .currentPeriodStartDate(now)
                .autoRenew(true)
                .build();
        log.info("Built new UserSubscription object (before status/dates). ID (pre-persist): {}. For Plan ID: {}", newSubscription.getId(), plan.getId());

        try {
        int trialDays = (plan.getTrialPeriodDays() != null) ? plan.getTrialPeriodDays() : 0;
        log.debug("Effective trial days for plan {}: {}", plan.getId(), trialDays);

        if (trialDays > 0) {
            log.debug("Setting status to TRIALING for new subscription");
            newSubscription.setStatus(UserSubscriptionStatus.TRIALING);
            log.debug("Calculating trial end date. now: {}, trialDays: {}", now, trialDays);
            newSubscription.setTrialEndDate(now.plus(trialDays, ChronoUnit.DAYS));
            log.debug("Trial end date set to: {}", newSubscription.getTrialEndDate());
            newSubscription.setCurrentPeriodEndDate(newSubscription.getTrialEndDate()); // First "bill" due after trial
            log.info("User {} starting trial for plan {}. Trial ends: {}. Current period ends: {}",
                     userId, plan.getId(), newSubscription.getTrialEndDate(), newSubscription.getCurrentPeriodEndDate());
        } else {
            log.debug("Setting status to PENDING_INITIAL_PAYMENT for new subscription");
            newSubscription.setStatus(UserSubscriptionStatus.PENDING_INITIAL_PAYMENT);
            log.debug("Calculating next billing date. now: {}, plan billing interval: {}, count: {}",
                      now, plan.getBillingInterval(), plan.getBillingIntervalCount());
            newSubscription.setCurrentPeriodEndDate(calculateNextBillingDate(now, plan));
            log.info("User {} starting subscription (no trial) for plan {}. Status: PENDING_INITIAL_PAYMENT. Next billing (current period end): {}",
                     userId, plan.getId(), newSubscription.getCurrentPeriodEndDate());
        }
        // ID for newSubscription will be set by its @PrePersist

        UserSubscription savedSubscription = userSubscriptionRepository.save(newSubscription);
        log.info("Saved initial UserSubscription with ID: {} and status: {}", savedSubscription.getId(), savedSubscription.getStatus());
        // eventService.recordEvent(savedSubscription.getId(), SubscriptionEventType.CREATED, "Subscription initiated.", "USER");

        // If not a trial or trial requires payment, initiate payment
        if (savedSubscription.getStatus() == UserSubscriptionStatus.PENDING_INITIAL_PAYMENT && plan.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            log.info("Attempting to initiate initial payment for subscription ID: {}. Plan Price: {}", savedSubscription.getId(), plan.getPrice());
            try {
                PaymentServiceClient.InitiatePaymentPayload payload = new PaymentServiceClient.InitiatePaymentPayload(
                        userId,
                        savedSubscription.getId(),
                        PaymentReferenceType.SUBSCRIPTION_INITIAL,
                        plan.getPrice(),
                        plan.getCurrency(),
                        PaymentGatewayType.STRIPE, // Or determine dynamically
                        "Initial payment for " + plan.getName(),
                        request.paymentMethodToken(),
                        null, // customerId - payment service might create/find this
                        request.storedPaymentMethodId(),
                        Map.of("subscription_plan_id", plan.getId(), "internal_user_subscription_id", savedSubscription.getId())
                );
                log.debug("Calling PaymentService with payload: {}", payload);
                ResponseEntity<PaymentServiceClient.PaymentServiceResponse> paymentResponse = paymentServiceClient.initiatePayment(payload);
                log.info("Received response from PaymentService for sub ID {}: Status Code {}", savedSubscription.getId(), paymentResponse.getStatusCode());

                if (paymentResponse.getStatusCode().is2xxSuccessful() && paymentResponse.getBody() != null) {
                    PaymentServiceClient.PaymentServiceResponse psResponse = paymentResponse.getBody();
                    log.info("Payment initiation successful for subscription {}. PaymentService Tx ID: {}, Gateway Status: {}",
                             savedSubscription.getId(), psResponse.id(), psResponse.status());

                    if ("SUCCEEDED".equalsIgnoreCase(psResponse.status())) {
                        // If payment service confirms immediate success (e.g. from mocked flow)
                        String StoredPaymentMethodIdForSubscription = request.storedPaymentMethodId() != null ? request.storedPaymentMethodId() : "new_card_via_" + psResponse.id(); // Placeholder
                        processInitialPaymentOutcome(savedSubscription.getId(), psResponse.id(), true, StoredPaymentMethodIdForSubscription);
                        // Update the local 'savedSubscription' variable with the outcome
                        savedSubscription = userSubscriptionRepository.findById(savedSubscription.getId()).orElse(savedSubscription);
                    } else if ("REQUIRES_ACTION".equalsIgnoreCase(psResponse.status()) || "PENDING_GATEWAY_ACTION".equalsIgnoreCase(psResponse.status())) {
                        log.info("Subscription {} requires further payment action from user.", savedSubscription.getId());
                        savedSubscription.setStatus(UserSubscriptionStatus.INCOMPLETE); // Update status
                        userSubscriptionRepository.save(savedSubscription); // Save this status change
                        // The UserSubscriptionResponse DTO might need to carry the clientSecret from psResponse.clientSecret()
                    } else {
                         log.warn("Initial payment for subscription {} initiated but not immediately successful or requiring action. Gateway Status: {}. Waiting for webhook.",
                                  savedSubscription.getId(), psResponse.status());
                         // Remains PENDING_INITIAL_PAYMENT or similar, relying on webhook from PaymentService.
                    }
                } else {
                    log.error("Failed to initiate payment for subscription {}. PaymentService response status: {}",
                              savedSubscription.getId(), paymentResponse.getStatusCode());
                    // Keep status as PENDING_INITIAL_PAYMENT, admin/system might need to review or user retry.
                    throw new PaymentProcessingException("Failed to initiate payment for subscription with PaymentService.");
                }
            } catch (FeignException fe) {
                log.error("FeignException during initial payment initiation for subscription {}: Status {}, Response: {}",
                          savedSubscription.getId(), fe.status(), fe.contentUTF8(), fe);
                throw new PaymentProcessingException("Communication error with PaymentService during payment initiation: " + fe.getMessage(), fe);
            } catch (Exception e) {
                log.error("Unexpected error during initial payment initiation for subscription {}: {}", savedSubscription.getId(), e.getMessage(), e);
                throw new PaymentProcessingException("Unexpected error initiating payment: " + e.getMessage(), e);
            }
        } else if (savedSubscription.getStatus() == UserSubscriptionStatus.TRIALING) {
            log.info("Subscription {} started in TRIALING state.", savedSubscription.getId());
            // eventService.recordEvent(savedSubscription.getId(), SubscriptionEventType.TRIAL_STARTED, "Trial period started.", "SYSTEM");
        } else if (plan.getPrice().compareTo(BigDecimal.ZERO) == 0) { // Free plan
            log.info("Activating free subscription {} for plan {}", savedSubscription.getId(), plan.getId());
            activateSubscription(savedSubscription, null, null); // No payment, no stored method
            // Update the local 'savedSubscription' variable
            savedSubscription = userSubscriptionRepository.findById(savedSubscription.getId()).orElse(savedSubscription);
        }

        return subscriptionMapper.toUserSubscriptionResponse(savedSubscription);
        } catch (Throwable t) {
    log.error("CRITICAL UNHANDLED EXCEPTION in SubscriptionServiceImpl.createUserSubscription for user {}: {}", userId, t.getMessage(), t); // THIS WILL PRINT THE STACK TRACE
    throw new RuntimeException("Critical error during subscription creation: " + t.getMessage(), t);
}
    }


    @Override
    @Transactional
    public void processInitialPaymentOutcome(String userSubscriptionId, String paymentTransactionId, boolean paymentSuccessful, String storedPaymentMethodIdUsed) {
        UserSubscription subscription = userSubscriptionRepository.findById(userSubscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("UserSubscription not found: " + userSubscriptionId));

        log.info("Processing initial payment outcome for subscription {}. Successful: {}. Payment Tx ID: {}",
                 userSubscriptionId, paymentSuccessful, paymentTransactionId);

        if (subscription.getStatus() != UserSubscriptionStatus.PENDING_INITIAL_PAYMENT && subscription.getStatus() != UserSubscriptionStatus.INCOMPLETE) {
            log.warn("Subscription {} is not in PENDING_INITIAL_PAYMENT or INCOMPLETE state. Current status: {}. Ignoring payment outcome.",
                     userSubscriptionId, subscription.getStatus());
            return;
        }

        if (paymentSuccessful) {
            activateSubscription(subscription, paymentTransactionId, storedPaymentMethodIdUsed);
        } else {
            subscription.setStatus(UserSubscriptionStatus.EXPIRED); // Or a specific "initial_payment_failed" status
            subscription.setEndDate(Instant.now());
            log.warn("Initial payment FAILED for subscription {}.", userSubscriptionId);
            // eventService.recordEvent(subscription.getId(), SubscriptionEventType.RENEWAL_FAILED, "Initial payment failed. Payment Tx: " + paymentTransactionId, "PAYMENT_GATEWAY");
        }
        userSubscriptionRepository.save(subscription);
    }

    private void activateSubscription(UserSubscription subscription, String paymentTransactionId, String storedPaymentMethodIdUsed) {
        SubscriptionPlan plan = planRepository.findById(subscription.getSubscriptionPlanId())
            .orElseThrow(() -> new IllegalStateException("Plan not found during activation for sub: " + subscription.getId())); // Should not happen if sub was created properly

        subscription.setStatus(UserSubscriptionStatus.ACTIVE);
        // If it was a trial ending, startDate might already be set.
        // If direct to active, startDate is now.
        if(subscription.getStartDate() == null || subscription.getStatus() == UserSubscriptionStatus.PENDING_INITIAL_PAYMENT) { // Check if was PENDING_INITIAL_PAYMENT before activate
            subscription.setStartDate(Instant.now());
        }
        subscription.setCurrentPeriodStartDate(Instant.now()); // Start of the first paid period (or after trial)
        subscription.setCurrentPeriodEndDate(calculateNextBillingDate(Instant.now(), plan));
        if (paymentTransactionId != null) {
            subscription.setLastSuccessfulPaymentId(paymentTransactionId);
        }
        if (storedPaymentMethodIdUsed != null) {
            subscription.setStoredPaymentMethodId(storedPaymentMethodIdUsed);
        }
        subscription.setTrialEndDate(null); // Clear trial end if moving to active
        log.info("Subscription {} ACTIVATED. Plan: {}. Next billing date: {}", subscription.getId(), plan.getName(), subscription.getCurrentPeriodEndDate());
        // eventService.recordEvent(subscription.getId(), SubscriptionEventType.ACTIVATED, "Subscription activated. Payment Tx: " + paymentTransactionId, "SYSTEM_OR_GATEWAY");
    }


    @Override
    @Transactional
    public void processRenewalPaymentOutcome(String userSubscriptionId, String paymentTransactionId, boolean paymentSuccessful) {
        UserSubscription subscription = userSubscriptionRepository.findById(userSubscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("UserSubscription not found: " + userSubscriptionId));
        SubscriptionPlan plan = planRepository.findById(subscription.getSubscriptionPlanId())
            .orElseThrow(() -> new IllegalStateException("Plan not found for sub: " + userSubscriptionId));

        log.info("Processing renewal payment outcome for subscription {}. Successful: {}. Payment Tx ID: {}",
                 userSubscriptionId, paymentSuccessful, paymentTransactionId);

        if (paymentSuccessful) {
            subscription.setStatus(UserSubscriptionStatus.ACTIVE);
            subscription.setLastSuccessfulPaymentId(paymentTransactionId);
            subscription.setCurrentPeriodStartDate(subscription.getCurrentPeriodEndDate()); // Old end date is new start
            subscription.setCurrentPeriodEndDate(calculateNextBillingDate(subscription.getCurrentPeriodStartDate(), plan));
            log.info("Subscription {} RENEWED successfully. Next billing date: {}", userSubscriptionId, subscription.getCurrentPeriodEndDate());
            // eventService.recordEvent(subscription.getId(), SubscriptionEventType.RENEWAL_SUCCESSFUL, "Payment Tx: " + paymentTransactionId, "SYSTEM_OR_GATEWAY");
        } else {
            subscription.setStatus(UserSubscriptionStatus.PAST_DUE);
            log.warn("Renewal payment FAILED for subscription {}. Status set to PAST_DUE.", userSubscriptionId);
            // TODO: Implement dunning logic (retry attempts, notifications to user)
            // eventService.recordEvent(subscription.getId(), SubscriptionEventType.RENEWAL_FAILED, "Payment Tx: " + paymentTransactionId, "SYSTEM_OR_GATEWAY");
        }
        userSubscriptionRepository.save(subscription);
    }


    @Override
    @Transactional
    public UserSubscriptionResponse cancelUserSubscription(String userId, String subscriptionId, String reason) {
        UserSubscription subscription = userSubscriptionRepository.findByIdAndUserId(subscriptionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("UserSubscription " + subscriptionId + " not found for user " + userId));
        log.info("User {} cancelling subscription {}. Current status: {}", userId, subscriptionId, subscription.getStatus());

        if (subscription.getStatus() == UserSubscriptionStatus.CANCELLED || subscription.getStatus() == UserSubscriptionStatus.EXPIRED) {
            log.warn("Subscription {} already cancelled or expired. No action taken.", subscriptionId);
            return subscriptionMapper.toUserSubscriptionResponse(subscription);
        }

        subscription.setStatus(UserSubscriptionStatus.CANCELLED);
        subscription.setAutoRenew(false);
        subscription.setCancelledAt(Instant.now());
        subscription.setCancellationReason(reason);
        // The subscription remains active until currentPeriodEndDate
        log.info("User {} cancelled subscription {}. It will remain usable until {} and then expire.",
                 userId, subscriptionId, subscription.getCurrentPeriodEndDate());

        // TODO: If using gateway-managed subscriptions (e.g. Stripe Subscriptions),
        // call the gateway API to set cancel_at_period_end=true or similar.

        UserSubscription savedSubscription = userSubscriptionRepository.save(subscription);
        // eventService.recordEvent(savedSubscription.getId(), SubscriptionEventType.CANCELLED_BY_USER, reason, "USER");
        return subscriptionMapper.toUserSubscriptionResponse(savedSubscription);
    }


    // @Scheduled(cron = "0 0 1 * * ?") // Example: Run daily at 1 AM
    @Override
    @Transactional
    public void processScheduledRenewals() {
        log.info("Starting scheduled subscription renewal process...");
        Instant now = Instant.now();
        List<UserSubscription> subscriptionsToRenew = userSubscriptionRepository
            .findByStatusAndAutoRenewAndCurrentPeriodEndDateLessThanEqual(UserSubscriptionStatus.ACTIVE, true, now);

        log.info("Found {} subscriptions due for renewal processing.", subscriptionsToRenew.size());

        for (UserSubscription sub : subscriptionsToRenew) {
            SubscriptionPlan plan = planRepository.findById(sub.getSubscriptionPlanId()).orElse(null);
            if (plan == null || plan.getStatus() != SubscriptionPlanStatus.ACTIVE) {
                log.warn("Subscription {} (User: {}) links to an inactive/missing plan {}. Cancelling subscription.",
                         sub.getId(), sub.getUserId(), sub.getSubscriptionPlanId());
                sub.setStatus(UserSubscriptionStatus.SYSTEM_CANCELLED); // Or EXPIRED
                sub.setCancellationReason("Associated plan is no longer active or available.");
                sub.setAutoRenew(false);
                sub.setEndDate(sub.getCurrentPeriodEndDate()); // Ends now or at period end
                userSubscriptionRepository.save(sub);
                // eventService.recordEvent(sub.getId(), SubscriptionEventType.CANCELLED_BY_SYSTEM, "Plan inactive.", "SYSTEM");
                continue;
            }

            if (sub.getStoredPaymentMethodId() == null) {
                log.warn("Subscription {} (User: {}) has no stored payment method for renewal. Moving to PAST_DUE.",
                         sub.getId(), sub.getUserId());
                sub.setStatus(UserSubscriptionStatus.PAST_DUE);
                userSubscriptionRepository.save(sub);
                // eventService.recordEvent(sub.getId(), SubscriptionEventType.RENEWAL_FAILED, "No stored payment method.", "SYSTEM");
                continue;
            }

            log.info("Attempting renewal for subscription {}. Plan: {}, Amount: {}", sub.getId(), plan.getName(), plan.getPrice());
            try {
                PaymentServiceClient.InitiatePaymentPayload payload = new PaymentServiceClient.InitiatePaymentPayload(
                        sub.getUserId(),
                        sub.getId(),
                        PaymentReferenceType.SUBSCRIPTION_RENEWAL,
                        plan.getPrice(),
                        plan.getCurrency(),
                        PaymentGatewayType.STRIPE, // Or determine from UserSubscription or Plan
                        "Renewal for " + plan.getName(),
                        null,
                        null,
                        sub.getStoredPaymentMethodId(),
                        Map.of("subscription_plan_id", plan.getId(), "renewal_attempt_at", now.toString())
                );
                ResponseEntity<PaymentServiceClient.PaymentServiceResponse> paymentResponse = paymentServiceClient.initiatePayment(payload);

                if (paymentResponse.getStatusCode().is2xxSuccessful() && paymentResponse.getBody() != null) {
                    PaymentServiceClient.PaymentServiceResponse psResponse = paymentResponse.getBody();
                    if ("SUCCEEDED".equalsIgnoreCase(psResponse.status())) {
                        log.info("Mocked renewal payment SUCCEEDED for subscription {} via PaymentService. Payment Tx: {}", sub.getId(), psResponse.id());
                        processRenewalPaymentOutcome(sub.getId(), psResponse.id(), true);
                    } else {
                        log.warn("Mocked renewal for sub {} via PaymentService didn't succeed immediately. Status: {}. Assuming webhook will follow or dunning needed.",
                                 sub.getId(), psResponse.status());
                        // If not SUCCEEDED, it might be PENDING_ACTION. Don't immediately mark as FAILED.
                        // Actual handling depends on payment service's synchronous response vs. webhook for renewals.
                        // For now, if not explicit success, we'll assume it failed for this mock scheduler.
                        if (!List.of("REQUIRES_ACTION", "PENDING_GATEWAY_ACTION", "PROCESSING").contains(psResponse.status().toUpperCase())) {
                             processRenewalPaymentOutcome(sub.getId(), psResponse.id(), false);
                        }
                    }
                } else {
                    log.error("Renewal payment initiation failed for subscription {}. PaymentService Response Status: {}",
                              sub.getId(), paymentResponse.getStatusCode());
                    processRenewalPaymentOutcome(sub.getId(), null, false);
                }
            } catch (Exception e) {
                log.error("Error during renewal payment for subscription {}: {}", sub.getId(), e.getMessage(), e);
                processRenewalPaymentOutcome(sub.getId(), null, false);
            }
        }
        log.info("Finished scheduled subscription renewal process.");
    }


    // Helper to calculate next billing date
    private Instant calculateNextBillingDate(Instant currentPeriodStartDate, SubscriptionPlan plan) {
        log.debug("Calculating next billing date from: {}, Plan Interval: {}, Count: {}",
                  currentPeriodStartDate, plan.getBillingInterval(), plan.getBillingIntervalCount());
        if (plan.getBillingInterval() == null) {
            log.error("BillingInterval is null for plan ID: {}", plan.getId());
            throw new IllegalStateException("BillingInterval cannot be null for plan: " + plan.getId());
        }
        if (plan.getBillingIntervalCount() <= 0) {
            log.error("BillingIntervalCount must be positive for plan ID: {}, got: {}", plan.getId(), plan.getBillingIntervalCount());
            throw new IllegalStateException("BillingIntervalCount must be positive for plan: " + plan.getId());
        }

          ZonedDateTime zdt = currentPeriodStartDate.atZone(ZoneId.systemDefault()); // Or ZoneId.of("UTC")
    ZonedDateTime nextZdt;

    long amountToAdd = plan.getBillingIntervalCount();

    switch (plan.getBillingInterval()) {
        case DAY:
            nextZdt = zdt.plusDays(amountToAdd);
            break;
        case WEEK:
            nextZdt = zdt.plusWeeks(amountToAdd);
            break;
        case MONTH:
            nextZdt = zdt.plusMonths(amountToAdd); // <<< USE ZonedDateTime's plusMonths
            break;
        case QUARTER:
            nextZdt = zdt.plusMonths(amountToAdd * 3); // <<< USE ZonedDateTime's plusMonths
            break;
        case YEAR:
            nextZdt = zdt.plusYears(amountToAdd); // <<< USE ZonedDateTime's plusYears
            break;
        default:
            log.error("Unsupported billing interval: {} for plan ID: {}", plan.getBillingInterval(), plan.getId());
            throw new IllegalArgumentException("Unsupported billing interval: " + plan.getBillingInterval());
    }
    return nextZdt.toInstant(); // Convert back to Instant
    }


    @Override
    @Transactional(readOnly = true)
    public UserSubscriptionResponse getUserSubscriptionById(String userId, String subscriptionId) {
        UserSubscription sub = userSubscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserSubscription " + subscriptionId + " not found for user " + userId));
        return subscriptionMapper.toUserSubscriptionResponse(sub);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSubscriptionResponse getActiveUserSubscriptionForPlan(String userId, String planId) {
        return userSubscriptionRepository
                .findByUserIdAndSubscriptionPlanIdAndStatusIn(userId, planId,
                    List.of(UserSubscriptionStatus.ACTIVE, UserSubscriptionStatus.TRIALING, UserSubscriptionStatus.PAST_DUE)) // PAST_DUE might still grant access for a bit
                .map(subscriptionMapper::toUserSubscriptionResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSubscriptionResponse> getAllUserSubscriptions(String userId) {
        return userSubscriptionRepository.findByUserIdOrderByStartDateDesc(userId).stream()
                .map(subscriptionMapper::toUserSubscriptionResponse)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public UserSubscriptionResponse reactivateUserSubscription(String userId, String subscriptionId) {
        UserSubscription subscription = userSubscriptionRepository.findByIdAndUserId(subscriptionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("UserSubscription " + subscriptionId + " not found for user " + userId));
        log.info("User {} reactivating subscription {}. Current status: {}", userId, subscriptionId, subscription.getStatus());

        if (subscription.getStatus() != UserSubscriptionStatus.CANCELLED) {
            throw new SubscriptionActionNotAllowedException("Subscription " + subscriptionId + " is not in CANCELLED state, cannot reactivate.");
        }
        if (subscription.getCurrentPeriodEndDate().isBefore(Instant.now())) {
             throw new SubscriptionActionNotAllowedException("Subscription " + subscriptionId + " billing period has already ended. Cannot reactivate this subscription; please create a new one.");
        }

        // Determine what state to return to. If was trialing and trialEndDate is still in future, could go back to trialing.
        // For simplicity, assume back to ACTIVE.
        subscription.setStatus(UserSubscriptionStatus.ACTIVE);
        subscription.setAutoRenew(true); // Turn auto-renew back on
        subscription.setCancelledAt(null);
        subscription.setCancellationReason(null);
        // endDate might be cleared if it was set to currentPeriodEndDate upon cancellation
        subscription.setEndDate(null);

        UserSubscription saved = userSubscriptionRepository.save(subscription);
        log.info("User {} reactivated subscription {}. Status set to ACTIVE.", userId, subscriptionId);
        // eventService.recordEvent(saved.getId(), SubscriptionEventType.STATUS_CHANGED, "Reactivated by user.", "USER");
        return subscriptionMapper.toUserSubscriptionResponse(saved);
    }
}