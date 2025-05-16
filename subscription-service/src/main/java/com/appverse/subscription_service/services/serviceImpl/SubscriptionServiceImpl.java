// === In Subscription Service Project ===
package com.appverse.subscription_service.services.serviceImpl;

import com.appverse.subscription_service.client.PaymentServiceClient;
import com.appverse.subscription_service.dto.*;
import com.appverse.subscription_service.enums.*;
import com.appverse.subscription_service.exception.*;
import com.appverse.subscription_service.mapper.SubscriptionMapper;
import com.appverse.subscription_service.model.SubscriptionPlan;
import com.appverse.subscription_service.model.UserSubscription;
import com.appverse.subscription_service.repository.SubscriptionPlanRepository;
import com.appverse.subscription_service.repository.UserSubscriptionRepository;
import com.appverse.subscription_service.services.SubscriptionService;

// Import event payloads (assuming they are created in this package or sub-package)
import com.appverse.subscription_service.event.payload.*;


import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate; // <<< KAFKA IMPORT
import org.springframework.kafka.support.SendResult;   // <<< KAFKA IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture; // <<< KAFKA IMPORT
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final SubscriptionMapper subscriptionMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate; // <<< KAFKA INJECTION

    private static final String SUBSCRIPTION_EVENTS_TOPIC = "subscription-events"; // <<< KAFKA TOPIC

    // Helper method for Kafka logging
    private void logKafkaSendOutcome(String eventName, String topic, String key, SendResult<String, Object> result, Throwable ex) {
        if (ex == null) {
            log.info("Successfully sent {} to topic {} for key {}: offset {}, partition {}",
                    eventName, topic, key,
                    result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
        } else {
            log.error("Failed to send {} to topic {} for key {}: {}",
                    eventName, topic, key, ex.getMessage(), ex);
        }
    }


    // --- Plan Management ---
    @Override
    @Transactional
    public SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request) {
        log.info("Admin creating new subscription plan: {}", request.name());
        if (planRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Subscription plan with name '" + request.name() + "' already exists.");
        }
        SubscriptionPlan plan = subscriptionMapper.toSubscriptionPlan(request);
        plan.setStatus(SubscriptionPlanStatus.ACTIVE);
        SubscriptionPlan savedPlan = planRepository.save(plan);
        log.info("SubscriptionPlan {} created with ID: {}", savedPlan.getName(), savedPlan.getId());

        // --- KAFKA EVENT ---
        PlanCreatedPayload payload = new PlanCreatedPayload(
                savedPlan.getId(), savedPlan.getName(), savedPlan.getDescription(),
                savedPlan.getPrice(), savedPlan.getCurrency(), savedPlan.getBillingInterval(),
                savedPlan.getBillingIntervalCount(), savedPlan.getTrialPeriodDays(),
                savedPlan.getStatus(), savedPlan.getApplicationId(), savedPlan.getDeveloperId(),
                savedPlan.getCreatedAt() != null ? savedPlan.getCreatedAt() : Instant.now() // Ensure createdAt is non-null
        );
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, savedPlan.getId(), payload);
        future.whenComplete((result, ex) -> logKafkaSendOutcome("PlanCreatedEvent", SUBSCRIPTION_EVENTS_TOPIC, savedPlan.getId(), result, ex));
        log.debug("Asynchronously published PlanCreatedEvent for Plan ID: {}.", savedPlan.getId());
        // --- END KAFKA EVENT ---

        return subscriptionMapper.toSubscriptionPlanResponse(savedPlan);
    }

    @Override
    @Transactional
    public SubscriptionPlanResponse updatePlan(String planId, SubscriptionPlanRequest request) {
        log.info("Admin updating subscription plan ID: {}", planId);
        SubscriptionPlan existingPlan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan not found with ID: " + planId));

        if (request.name() != null && !existingPlan.getName().equals(request.name()) && planRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Subscription plan with name '" + request.name() + "' already exists.");
        }
        subscriptionMapper.updateSubscriptionPlanFromRequest(request, existingPlan);
        SubscriptionPlan updatedPlan = planRepository.save(existingPlan);
        log.info("SubscriptionPlan {} updated.", updatedPlan.getId());

        // --- KAFKA EVENT ---
        PlanUpdatedPayload payload = new PlanUpdatedPayload(
                updatedPlan.getId(), updatedPlan.getName(), updatedPlan.getDescription(),
                updatedPlan.getPrice(), updatedPlan.getCurrency(), updatedPlan.getBillingInterval(),
                updatedPlan.getBillingIntervalCount(), updatedPlan.getTrialPeriodDays(),
                updatedPlan.getStatus(), updatedPlan.getApplicationId(), updatedPlan.getDeveloperId(),
                updatedPlan.getUpdatedAt() != null ? updatedPlan.getUpdatedAt() : Instant.now() // Ensure updatedAt is non-null
        );
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, updatedPlan.getId(), payload);
        future.whenComplete((result, ex) -> logKafkaSendOutcome("PlanUpdatedEvent", SUBSCRIPTION_EVENTS_TOPIC, updatedPlan.getId(), result, ex));
        log.debug("Asynchronously published PlanUpdatedEvent for Plan ID: {}.", updatedPlan.getId());
        // --- END KAFKA EVENT ---

        return subscriptionMapper.toSubscriptionPlanResponse(updatedPlan);
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
                // .createdAt(Instant.now()) // Assuming @PrePersist handles this in entity
                // .updatedAt(Instant.now()) // Assuming @PrePersist/@PreUpdate handles this
                .build();

        SubscriptionPlan savedPlan = planRepository.save(plan);
        log.info("Developer-defined SubscriptionPlan '{}' created with ID {} for app {} by dev {}",
                 savedPlan.getName(), savedPlan.getId(), savedPlan.getApplicationId(), savedPlan.getDeveloperId());
        
        // --- KAFKA EVENT (same as regular plan creation for now) ---
        PlanCreatedPayload payload = new PlanCreatedPayload(
                savedPlan.getId(), savedPlan.getName(), savedPlan.getDescription(),
                savedPlan.getPrice(), savedPlan.getCurrency(), savedPlan.getBillingInterval(),
                savedPlan.getBillingIntervalCount(), savedPlan.getTrialPeriodDays(),
                savedPlan.getStatus(), savedPlan.getApplicationId(), savedPlan.getDeveloperId(),
                savedPlan.getCreatedAt() != null ? savedPlan.getCreatedAt() : Instant.now()
        );
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, savedPlan.getId(), payload);
        future.whenComplete((result, ex) -> logKafkaSendOutcome("PlanCreatedEvent (Developer)", SUBSCRIPTION_EVENTS_TOPIC, savedPlan.getId(), result, ex));
        log.debug("Asynchronously published PlanCreatedEvent for Developer Plan ID: {}.", savedPlan.getId());
        // --- END KAFKA EVENT ---

        return subscriptionMapper.toSubscriptionPlanResponse(savedPlan);
    }


    @Override
    @Transactional
    public void deactivatePlan(String planId) {
        log.info("Admin deactivating plan ID: {}", planId);
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan not found with ID: " + planId));
        plan.setStatus(SubscriptionPlanStatus.INACTIVE);
        SubscriptionPlan savedPlan = planRepository.save(plan); // Save to get potential updatedAt timestamp
        log.info("SubscriptionPlan {} deactivated.", planId);

        // --- KAFKA EVENT ---
        PlanDeactivatedPayload payload = new PlanDeactivatedPayload(
                savedPlan.getId(),
                savedPlan.getName(),
                savedPlan.getUpdatedAt() != null ? savedPlan.getUpdatedAt() : Instant.now()
        );
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, savedPlan.getId(), payload);
        future.whenComplete((result, ex) -> logKafkaSendOutcome("PlanDeactivatedEvent", SUBSCRIPTION_EVENTS_TOPIC, savedPlan.getId(), result, ex));
        log.debug("Asynchronously published PlanDeactivatedEvent for Plan ID: {}.", savedPlan.getId());
        // --- END KAFKA EVENT ---
    }

    @Override
    @Transactional
    public void activatePlan(String planId) {
        log.info("Admin activating plan ID: {}", planId);
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan not found with ID: " + planId));
        plan.setStatus(SubscriptionPlanStatus.ACTIVE);
        SubscriptionPlan savedPlan = planRepository.save(plan);
        log.info("SubscriptionPlan {} activated.", planId);

        // --- KAFKA EVENT ---
        PlanActivatedPayload payload = new PlanActivatedPayload(
                savedPlan.getId(),
                savedPlan.getName(),
                savedPlan.getUpdatedAt() != null ? savedPlan.getUpdatedAt() : Instant.now()
        );
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, savedPlan.getId(), payload);
        future.whenComplete((result, ex) -> logKafkaSendOutcome("PlanActivatedEvent", SUBSCRIPTION_EVENTS_TOPIC, savedPlan.getId(), result, ex));
        log.debug("Asynchronously published PlanActivatedEvent for Plan ID: {}.", savedPlan.getId());
        // --- END KAFKA EVENT ---
    }

    // --- User Subscription Management ---
    @Override
    @Transactional
    public UserSubscriptionResponse createUserSubscription(String userId, CreateUserSubscriptionRequest request) {
        log.info("User {} attempting to subscribe to plan ID: {}", userId, request.subscriptionPlanId());

        SubscriptionPlan plan = planRepository.findByIdAndStatus(request.subscriptionPlanId(), SubscriptionPlanStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active SubscriptionPlan not found or invalid: " + request.subscriptionPlanId()));

        // ... (existing logic for checking conflicts) ...
        Optional<UserSubscription> existingActiveSub = userSubscriptionRepository
            .findByUserIdAndSubscriptionPlanIdAndStatusIn(userId, plan.getId(),
                List.of(UserSubscriptionStatus.ACTIVE, UserSubscriptionStatus.TRIALING, UserSubscriptionStatus.PAST_DUE, UserSubscriptionStatus.PENDING_INITIAL_PAYMENT, UserSubscriptionStatus.INCOMPLETE));
        if (existingActiveSub.isPresent()) {
            log.warn("User {} already has an active, trialing, pending, or past_due subscription (ID: {}) to plan {}.",
                     userId, existingActiveSub.get().getId(), plan.getId());
            throw new SubscriptionActionNotAllowedException("User already has an active, trialing, pending, or past_due subscription to this plan.");
        }


        Instant now = Instant.now();
        UserSubscription newSubscription = UserSubscription.builder()
                .userId(userId)
                .subscriptionPlanId(plan.getId())
                .startDate(now) // Tentative, might be overwritten by trial logic or activation
                .currentPeriodStartDate(now) // Tentative
                .autoRenew(true)
                .build();

        int trialDays = (plan.getTrialPeriodDays() != null) ? plan.getTrialPeriodDays() : 0;
        if (trialDays > 0) {
            newSubscription.setStatus(UserSubscriptionStatus.TRIALING);
            newSubscription.setTrialEndDate(now.plus(trialDays, ChronoUnit.DAYS));
            newSubscription.setCurrentPeriodEndDate(newSubscription.getTrialEndDate());
        } else {
            newSubscription.setStatus(UserSubscriptionStatus.PENDING_INITIAL_PAYMENT);
            newSubscription.setCurrentPeriodEndDate(calculateNextBillingDate(now, plan));
        }
        // ID for newSubscription will be set by its @PrePersist if applicable, or after save.
        // Make sure createdAt is set by @PrePersist or here.
        // newSubscription.setCreatedAt(now); // If not handled by @PrePersist

        UserSubscription savedSubscription = userSubscriptionRepository.save(newSubscription);
        log.info("Saved initial UserSubscription with ID: {} and status: {}", savedSubscription.getId(), savedSubscription.getStatus());

        // --- KAFKA EVENT: SubscriptionInitiated ---
        SubscriptionInitiatedPayload initiatedPayload = new SubscriptionInitiatedPayload(
                savedSubscription.getId(),
                savedSubscription.getUserId(),
                savedSubscription.getSubscriptionPlanId(),
                plan.getName(), // Plan name for convenience
                savedSubscription.getStatus(),
                savedSubscription.getStartDate(),
                savedSubscription.getTrialEndDate(),
                savedSubscription.getCurrentPeriodEndDate(),
                savedSubscription.isAutoRenew(),
                savedSubscription.getCreatedAt() != null ? savedSubscription.getCreatedAt() : now
        );
        CompletableFuture<SendResult<String, Object>> initiatedFuture =
                kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, savedSubscription.getId(), initiatedPayload);
        initiatedFuture.whenComplete((res, e) -> logKafkaSendOutcome("SubscriptionInitiatedEvent", SUBSCRIPTION_EVENTS_TOPIC, savedSubscription.getId(), res, e));
        log.debug("Asynchronously published SubscriptionInitiatedEvent for Subscription ID: {}.", savedSubscription.getId());
        // --- END KAFKA EVENT ---

        if (savedSubscription.getStatus() == UserSubscriptionStatus.PENDING_INITIAL_PAYMENT && plan.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            log.info("Attempting to initiate initial payment for subscription ID: {}. Plan Price: {}", savedSubscription.getId(), plan.getPrice());
            try {
                // ... (payment initiation logic) ...
                PaymentServiceClient.InitiatePaymentPayload paymentPayload = new PaymentServiceClient.InitiatePaymentPayload(
                        userId, savedSubscription.getId(), PaymentReferenceType.SUBSCRIPTION_INITIAL,
                        plan.getPrice(), plan.getCurrency(), PaymentGatewayType.STRIPE,
                        "Initial payment for " + plan.getName(), request.paymentMethodToken(),
                        null, request.storedPaymentMethodId(),
                        Map.of("subscription_plan_id", plan.getId(), "internal_user_subscription_id", savedSubscription.getId())
                );
                ResponseEntity<PaymentServiceClient.PaymentServiceResponse> paymentResponse = paymentServiceClient.initiatePayment(paymentPayload);

                if (paymentResponse.getStatusCode().is2xxSuccessful() && paymentResponse.getBody() != null) {
                    PaymentServiceClient.PaymentServiceResponse psResponse = paymentResponse.getBody();
                    if ("SUCCEEDED".equalsIgnoreCase(psResponse.status())) {
                        String storedPaymentMethodIdForSubscription = request.storedPaymentMethodId() != null ? request.storedPaymentMethodId() : "new_card_via_" + psResponse.id();
                        processInitialPaymentOutcome(savedSubscription.getId(), psResponse.id(), true, storedPaymentMethodIdForSubscription);
                        final UserSubscription refreshedSubscription = userSubscriptionRepository.findById(savedSubscription.getId()).orElse(savedSubscription); // Refresh
                    } else if ("REQUIRES_ACTION".equalsIgnoreCase(psResponse.status()) || "PENDING_GATEWAY_ACTION".equalsIgnoreCase(psResponse.status())) {
                        log.info("Subscription {} requires further payment action from user. Client Secret: {}", savedSubscription.getId(), psResponse.clientSecret());
                        savedSubscription.setStatus(UserSubscriptionStatus.INCOMPLETE);
                        userSubscriptionRepository.save(savedSubscription);
                        // --- KAFKA EVENT: SubscriptionRequiresPaymentAction ---
                        SubscriptionRequiresPaymentActionPayload requiresActionPayload = new SubscriptionRequiresPaymentActionPayload(
                                savedSubscription.getId(), savedSubscription.getUserId(), savedSubscription.getSubscriptionPlanId(),
                                psResponse.clientSecret(), "Payment requires further user action.", Instant.now()
                        );
                        CompletableFuture<SendResult<String, Object>> requiresActionFuture =
                                kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, savedSubscription.getId(), requiresActionPayload);
                        requiresActionFuture.whenComplete((r, ex) -> logKafkaSendOutcome("SubscriptionRequiresPaymentActionEvent", SUBSCRIPTION_EVENTS_TOPIC, savedSubscription.getId(), r, ex));
                        log.debug("Asynchronously published SubscriptionRequiresPaymentActionEvent for Sub ID: {}.", savedSubscription.getId());
                        // --- END KAFKA EVENT ---
                    } else {
                         log.warn("Initial payment for subscription {} initiated but not SUCCEEDED or REQUIRES_ACTION. Gateway Status: {}.",
                                  savedSubscription.getId(), psResponse.status());
                         // No specific event here, relies on webhook for other statuses. The SubscriptionInitiatedEvent is already sent.
                    }
                } else {
                    log.error("Failed to initiate payment for subscription {}. PaymentService response status: {}", savedSubscription.getId(), paymentResponse.getStatusCode());
                    // This will likely throw PaymentProcessingException later, no event here unless we change status.
                    // If it results in a failed state for the subscription, processInitialPaymentOutcome(false) should be called.
                    // For now, an exception is thrown.
                    throw new PaymentProcessingException("Failed to initiate payment for subscription with PaymentService. Status: " + paymentResponse.getStatusCode());
                }
            } catch (FeignException fe) {
                log.error("FeignException during initial payment initiation for subscription {}: Status {}, Response: {}", savedSubscription.getId(), fe.status(), fe.contentUTF8(), fe);
                throw new PaymentProcessingException("Communication error with PaymentService during payment initiation: " + fe.getMessage(), fe);
            } catch (Exception e) {
                log.error("Unexpected error during initial payment initiation for subscription {}: {}", savedSubscription.getId(), e.getMessage(), e);
                // If this error means the subscription should fail, we might need to call processInitialPaymentOutcome(false)
                // or set a specific failed status and send an event. For now, it throws.
                throw new PaymentProcessingException("Unexpected error initiating payment: " + e.getMessage(), e);
            }
        } else if (savedSubscription.getStatus() == UserSubscriptionStatus.TRIALING) {
            log.info("Subscription {} started in TRIALING state.", savedSubscription.getId());
            // The SubscriptionInitiatedEvent with status TRIALING covers this.
        } else if (plan.getPrice().compareTo(BigDecimal.ZERO) == 0) { // Free plan
            log.info("Activating free subscription {} for plan {}", savedSubscription.getId(), plan.getId());
            activateSubscription(savedSubscription, null, null);
            final UserSubscription refreshedSubscription = userSubscriptionRepository.findById(savedSubscription.getId()).orElse(savedSubscription); // Refresh after activation
            // The SubscriptionActivatedEvent will be sent by activateSubscription or its wrapper logic.
        }
        return subscriptionMapper.toUserSubscriptionResponse(savedSubscription);
    }


    @Override
    @Transactional
    public void processInitialPaymentOutcome(String userSubscriptionId, String paymentTransactionId, boolean paymentSuccessful, String storedPaymentMethodIdUsed) {
        UserSubscription subscription = userSubscriptionRepository.findById(userSubscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("UserSubscription not found: " + userSubscriptionId));
        SubscriptionPlan plan = planRepository.findById(subscription.getSubscriptionPlanId())
            .orElseThrow(() -> new IllegalStateException("Plan not found for sub: " + userSubscriptionId)); // Should not happen

        log.info("Processing initial payment outcome for subscription {}. Successful: {}. Payment Tx ID: {}",
                 userSubscriptionId, paymentSuccessful, paymentTransactionId);

        if (subscription.getStatus() != UserSubscriptionStatus.PENDING_INITIAL_PAYMENT && subscription.getStatus() != UserSubscriptionStatus.INCOMPLETE) {
            log.warn("Subscription {} is not in PENDING_INITIAL_PAYMENT or INCOMPLETE state. Current status: {}. Ignoring payment outcome.",
                     userSubscriptionId, subscription.getStatus());
            return;
        }

        if (paymentSuccessful) {
            activateSubscription(subscription, paymentTransactionId, storedPaymentMethodIdUsed);
            // 'subscription' object is updated by activateSubscription and saved within its transaction context (or this one).
            // --- KAFKA EVENT: SubscriptionActivated ---
            SubscriptionActivatedPayload activatedPayload = new SubscriptionActivatedPayload(
                    subscription.getId(), subscription.getUserId(), subscription.getSubscriptionPlanId(),
                    plan.getName(), subscription.getStartDate(), subscription.getCurrentPeriodStartDate(),
                    subscription.getCurrentPeriodEndDate(), subscription.getLastSuccessfulPaymentId(),
                    subscription.getStoredPaymentMethodId(),
                    subscription.getUpdatedAt() != null ? subscription.getUpdatedAt() : Instant.now() // Or just Instant.now()
            );
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, subscription.getId(), activatedPayload);
            future.whenComplete((r, e) -> logKafkaSendOutcome("SubscriptionActivatedEvent", SUBSCRIPTION_EVENTS_TOPIC, subscription.getId(), r, e));
            log.debug("Asynchronously published SubscriptionActivatedEvent for Sub ID: {}.", subscription.getId());
            // --- END KAFKA EVENT ---
        } else {
            subscription.setStatus(UserSubscriptionStatus.EXPIRED); // Or a specific "initial_payment_failed" status
            subscription.setEndDate(Instant.now());
            // subscription.setUpdatedAt(Instant.now()); // If entity has this field
            userSubscriptionRepository.save(subscription); // Explicit save as status changed without full activation flow
            log.warn("Initial payment FAILED for subscription {}.", userSubscriptionId);

            // --- KAFKA EVENT: SubscriptionInitialPaymentFailed ---
            SubscriptionInitialPaymentFailedPayload failurePayload = new SubscriptionInitialPaymentFailedPayload(
                    subscription.getId(), subscription.getUserId(), subscription.getSubscriptionPlanId(),
                    paymentTransactionId, "Initial payment processing failed.",
                    subscription.getUpdatedAt() != null ? subscription.getUpdatedAt() : Instant.now() // Or just Instant.now()
            );
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, subscription.getId(), failurePayload);
            future.whenComplete((r, e) -> logKafkaSendOutcome("SubscriptionInitialPaymentFailedEvent", SUBSCRIPTION_EVENTS_TOPIC, subscription.getId(), r, e));
            log.debug("Asynchronously published SubscriptionInitialPaymentFailedEvent for Sub ID: {}.", subscription.getId());
            // --- END KAFKA EVENT ---
        }
        // userSubscriptionRepository.save(subscription); // Done by activateSubscription or above if failed
    }

    // Private method, called within a @Transactional context
    private void activateSubscription(UserSubscription subscription, String paymentTransactionId, String storedPaymentMethodIdUsed) {
        SubscriptionPlan plan = planRepository.findById(subscription.getSubscriptionPlanId())
            .orElseThrow(() -> new IllegalStateException("Plan not found during activation for sub: " + subscription.getId()));

        subscription.setStatus(UserSubscriptionStatus.ACTIVE);
        Instant activationTime = Instant.now();
        if(subscription.getStartDate() == null || subscription.getStatus() == UserSubscriptionStatus.PENDING_INITIAL_PAYMENT) {
            subscription.setStartDate(activationTime);
        }
        subscription.setCurrentPeriodStartDate(activationTime);
        subscription.setCurrentPeriodEndDate(calculateNextBillingDate(activationTime, plan));
        if (paymentTransactionId != null) {
            subscription.setLastSuccessfulPaymentId(paymentTransactionId);
        }
        if (storedPaymentMethodIdUsed != null) {
            subscription.setStoredPaymentMethodId(storedPaymentMethodIdUsed);
        }
        subscription.setTrialEndDate(null);
        // subscription.setUpdatedAt(activationTime); // If entity manages this field
        // The save will happen by the transactional context of the calling public method.
        // Or if this method were @Transactional(propagation = Propagation.REQUIRES_NEW), it would save itself.
        // For simplicity, assume calling method's transaction handles the save.
        // If this private method does a save: userSubscriptionRepository.save(subscription);
        log.info("Subscription {} properties set for ACTIVATION. Plan: {}. Next billing date: {}", subscription.getId(), plan.getName(), subscription.getCurrentPeriodEndDate());
        // Event sending is handled by the public callers of this method after it completes.
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
        Instant eventTimestamp = Instant.now();

        if (paymentSuccessful) {
            subscription.setStatus(UserSubscriptionStatus.ACTIVE);
            subscription.setLastSuccessfulPaymentId(paymentTransactionId);
            subscription.setCurrentPeriodStartDate(subscription.getCurrentPeriodEndDate()); // Old end date is new start
            subscription.setCurrentPeriodEndDate(calculateNextBillingDate(subscription.getCurrentPeriodStartDate(), plan));
            // subscription.setUpdatedAt(eventTimestamp);
            log.info("Subscription {} RENEWED successfully. Next billing date: {}", userSubscriptionId, subscription.getCurrentPeriodEndDate());

            // --- KAFKA EVENT: SubscriptionRenewalSuccessful ---
            SubscriptionRenewalSuccessfulPayload successPayload = new SubscriptionRenewalSuccessfulPayload(
                    subscription.getId(), subscription.getUserId(), subscription.getSubscriptionPlanId(),
                    subscription.getCurrentPeriodStartDate(), subscription.getCurrentPeriodEndDate(),
                    subscription.getLastSuccessfulPaymentId(),
                    eventTimestamp
            );
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, subscription.getId(), successPayload);
            future.whenComplete((r, e) -> logKafkaSendOutcome("SubscriptionRenewalSuccessfulEvent", SUBSCRIPTION_EVENTS_TOPIC, subscription.getId(), r, e));
            log.debug("Asynchronously published SubscriptionRenewalSuccessfulEvent for Sub ID: {}.", subscription.getId());
            // --- END KAFKA EVENT ---
        } else {
            subscription.setStatus(UserSubscriptionStatus.PAST_DUE);
            // subscription.setUpdatedAt(eventTimestamp);
            log.warn("Renewal payment FAILED for subscription {}. Status set to PAST_DUE.", userSubscriptionId);

            // --- KAFKA EVENT: SubscriptionRenewalFailed ---
            SubscriptionRenewalFailedPayload failurePayload = new SubscriptionRenewalFailedPayload(
                    subscription.getId(), subscription.getUserId(), subscription.getSubscriptionPlanId(),
                    paymentTransactionId, "Renewal payment processing failed.",
                    eventTimestamp
            );
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, subscription.getId(), failurePayload);
            future.whenComplete((r, e) -> logKafkaSendOutcome("SubscriptionRenewalFailedEvent", SUBSCRIPTION_EVENTS_TOPIC, subscription.getId(), r, e));
            log.debug("Asynchronously published SubscriptionRenewalFailedEvent for Sub ID: {}.", subscription.getId());
            // --- END KAFKA EVENT ---
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
        Instant cancellationTime = Instant.now();
        subscription.setStatus(UserSubscriptionStatus.CANCELLED);
        subscription.setAutoRenew(false);
        subscription.setCancelledAt(cancellationTime);
        subscription.setCancellationReason(reason);
        // subscription.setUpdatedAt(cancellationTime);

        UserSubscription savedSubscription = userSubscriptionRepository.save(subscription);
        log.info("User {} cancelled subscription {}. It will remain usable until {} and then expire.",
                 userId, subscriptionId, savedSubscription.getCurrentPeriodEndDate());

        // --- KAFKA EVENT: SubscriptionCancelledByUser ---
        SubscriptionCancelledByUserPayload payload = new SubscriptionCancelledByUserPayload(
                savedSubscription.getId(), savedSubscription.getUserId(), savedSubscription.getSubscriptionPlanId(),
                savedSubscription.getCancellationReason(), savedSubscription.getCancelledAt(),
                savedSubscription.getCurrentPeriodEndDate()
        );
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, savedSubscription.getId(), payload);
        future.whenComplete((r, e) -> logKafkaSendOutcome("SubscriptionCancelledByUserEvent", SUBSCRIPTION_EVENTS_TOPIC, savedSubscription.getId(), r, e));
        log.debug("Asynchronously published SubscriptionCancelledByUserEvent for Sub ID: {}.", savedSubscription.getId());
        // --- END KAFKA EVENT ---

        return subscriptionMapper.toUserSubscriptionResponse(savedSubscription);
    }


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
            Instant eventTimestamp = Instant.now();

            if (plan == null || plan.getStatus() != SubscriptionPlanStatus.ACTIVE) {
                log.warn("Subscription {} (User: {}) links to an inactive/missing plan {}. Cancelling subscription.",
                         sub.getId(), sub.getUserId(), sub.getSubscriptionPlanId());
                String reason = "Associated plan is no longer active or available.";
                sub.setStatus(UserSubscriptionStatus.SYSTEM_CANCELLED);
                sub.setCancellationReason(reason);
                sub.setAutoRenew(false);
                sub.setEndDate(sub.getCurrentPeriodEndDate());
                // sub.setUpdatedAt(eventTimestamp);
                userSubscriptionRepository.save(sub);

                // --- KAFKA EVENT: SubscriptionSystemCancelled ---
                SubscriptionSystemCancelledPayload payload = new SubscriptionSystemCancelledPayload(
                        sub.getId(), sub.getUserId(), sub.getSubscriptionPlanId(), reason, eventTimestamp
                );
                CompletableFuture<SendResult<String, Object>> future =
                        kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, sub.getId(), payload);
                future.whenComplete((r, e) -> logKafkaSendOutcome("SubscriptionSystemCancelledEvent", SUBSCRIPTION_EVENTS_TOPIC, sub.getId(), r, e));
                log.debug("Asynchronously published SubscriptionSystemCancelledEvent for Sub ID: {}.", sub.getId());
                // --- END KAFKA EVENT ---
                continue;
            }

            if (sub.getStoredPaymentMethodId() == null) {
                log.warn("Subscription {} (User: {}) has no stored payment method for renewal. Moving to PAST_DUE.",
                         sub.getId(), sub.getUserId());
                sub.setStatus(UserSubscriptionStatus.PAST_DUE);
                // sub.setUpdatedAt(eventTimestamp);
                userSubscriptionRepository.save(sub);

                // --- KAFKA EVENT: SubscriptionRenewalFailed (No Payment Method) ---
                SubscriptionRenewalFailedPayload failurePayload = new SubscriptionRenewalFailedPayload(
                        sub.getId(), sub.getUserId(), sub.getSubscriptionPlanId(),
                        null, "No stored payment method for renewal.", eventTimestamp
                );
                CompletableFuture<SendResult<String, Object>> future =
                        kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, sub.getId(), failurePayload);
                future.whenComplete((r, e) -> logKafkaSendOutcome("SubscriptionRenewalFailedEvent", SUBSCRIPTION_EVENTS_TOPIC, sub.getId(), r, e));
                log.debug("Asynchronously published SubscriptionRenewalFailedEvent (No PM) for Sub ID: {}.", sub.getId());
                // --- END KAFKA EVENT ---
                continue;
            }

            log.info("Attempting renewal for subscription {}. Plan: {}, Amount: {}", sub.getId(), plan.getName(), plan.getPrice());
            try {
                // ... (payment initiation logic for renewal) ...
                PaymentServiceClient.InitiatePaymentPayload paymentPayload = new PaymentServiceClient.InitiatePaymentPayload(
                        sub.getUserId(), sub.getId(), PaymentReferenceType.SUBSCRIPTION_RENEWAL,
                        plan.getPrice(), plan.getCurrency(), PaymentGatewayType.STRIPE,
                        "Renewal for " + plan.getName(), null, null, sub.getStoredPaymentMethodId(),
                        Map.of("subscription_plan_id", plan.getId(), "renewal_attempt_at", now.toString())
                );
                ResponseEntity<PaymentServiceClient.PaymentServiceResponse> paymentResponse = paymentServiceClient.initiatePayment(paymentPayload);

                if (paymentResponse.getStatusCode().is2xxSuccessful() && paymentResponse.getBody() != null) {
                    PaymentServiceClient.PaymentServiceResponse psResponse = paymentResponse.getBody();
                    if ("SUCCEEDED".equalsIgnoreCase(psResponse.status())) {
                        processRenewalPaymentOutcome(sub.getId(), psResponse.id(), true); // This sends SubscriptionRenewalSuccessfulEvent
                    } else {
                        log.warn("Mocked renewal for sub {} via PaymentService didn't succeed immediately. Status: {}. Assuming webhook or dunning.",
                                 sub.getId(), psResponse.status());
                        if (!List.of("REQUIRES_ACTION", "PENDING_GATEWAY_ACTION", "PROCESSING").contains(psResponse.status().toUpperCase())) {
                             processRenewalPaymentOutcome(sub.getId(), psResponse.id(), false); // This sends SubscriptionRenewalFailedEvent
                        }
                        // If REQUIRES_ACTION, a different event or handling might be needed (e.g., SubscriptionRequiresPaymentActionPayload)
                        // For now, rely on processRenewalPaymentOutcome to handle the direct failure.
                    }
                } else {
                    log.error("Renewal payment initiation failed for subscription {}. PaymentService Response Status: {}",
                              sub.getId(), paymentResponse.getStatusCode());
                    processRenewalPaymentOutcome(sub.getId(), null, false); // This sends SubscriptionRenewalFailedEvent
                }
            } catch (Exception e) {
                log.error("Error during renewal payment for subscription {}: {}", sub.getId(), e.getMessage(), e);
                processRenewalPaymentOutcome(sub.getId(), null, false); // This sends SubscriptionRenewalFailedEvent
            }
        }
        log.info("Finished scheduled subscription renewal process.");
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
             throw new SubscriptionActionNotAllowedException("Subscription " + subscriptionId + " billing period has already ended. Cannot reactivate; please create a new one.");
        }
        Instant reactivationTime = Instant.now();
        subscription.setStatus(UserSubscriptionStatus.ACTIVE);
        subscription.setAutoRenew(true);
        subscription.setCancelledAt(null);
        subscription.setCancellationReason(null);
        subscription.setEndDate(null);
        // subscription.setUpdatedAt(reactivationTime);

        UserSubscription savedSubscription = userSubscriptionRepository.save(subscription);
        log.info("User {} reactivated subscription {}. Status set to ACTIVE.", userId, subscriptionId);

        // --- KAFKA EVENT: SubscriptionReactivatedByUser ---
        SubscriptionReactivatedByUserPayload payload = new SubscriptionReactivatedByUserPayload(
                savedSubscription.getId(), savedSubscription.getUserId(), savedSubscription.getSubscriptionPlanId(),
                reactivationTime // or savedSubscription.getUpdatedAt()
        );
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(SUBSCRIPTION_EVENTS_TOPIC, savedSubscription.getId(), payload);
        future.whenComplete((r, e) -> logKafkaSendOutcome("SubscriptionReactivatedByUserEvent", SUBSCRIPTION_EVENTS_TOPIC, savedSubscription.getId(), r, e));
        log.debug("Asynchronously published SubscriptionReactivatedByUserEvent for Sub ID: {}.", savedSubscription.getId());
        // --- END KAFKA EVENT ---

        return subscriptionMapper.toUserSubscriptionResponse(savedSubscription);
    }


    // --- Read-only methods (no Kafka events) ---
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
                    List.of(UserSubscriptionStatus.ACTIVE, UserSubscriptionStatus.TRIALING, UserSubscriptionStatus.PAST_DUE))
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

        ZonedDateTime zdt = currentPeriodStartDate.atZone(ZoneId.systemDefault());
        ZonedDateTime nextZdt;
        long amountToAdd = plan.getBillingIntervalCount();

        switch (plan.getBillingInterval()) {
            case DAY: nextZdt = zdt.plusDays(amountToAdd); break;
            case WEEK: nextZdt = zdt.plusWeeks(amountToAdd); break;
            case MONTH: nextZdt = zdt.plusMonths(amountToAdd); break;
            case QUARTER: nextZdt = zdt.plusMonths(amountToAdd * 3); break;
            case YEAR: nextZdt = zdt.plusYears(amountToAdd); break;
            default:
                log.error("Unsupported billing interval: {} for plan ID: {}", plan.getBillingInterval(), plan.getId());
                throw new IllegalArgumentException("Unsupported billing interval: " + plan.getBillingInterval());
        }
        return nextZdt.toInstant();
    }
}