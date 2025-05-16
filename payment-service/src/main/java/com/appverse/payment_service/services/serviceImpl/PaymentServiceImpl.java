// === In Payment Service Project ===
package com.appverse.payment_service.services.serviceImpl; // Corrected typo: services.serviceImpl

import com.appverse.payment_service.dto.*;
import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentMethodType;
import com.appverse.payment_service.enums.PaymentReferenceType;
import com.appverse.payment_service.enums.PaymentTransactionStatus;
import com.appverse.payment_service.enums.StoredPaymentMethodStatus;
import com.appverse.payment_service.event.payload.*;
import com.appverse.payment_service.exception.PaymentProcessingException;
import com.appverse.payment_service.exception.ResourceNotFoundException;
import com.appverse.payment_service.mapper.PaymentMapper;
import com.appverse.payment_service.model.PaymentTransaction;
import com.appverse.payment_service.model.StoredPaymentMethod;
import com.appverse.payment_service.repository.PaymentTransactionRepository;
import com.appverse.payment_service.repository.StoredPaymentMethodRepository;
import com.appverse.payment_service.services.PaymentService; // Corrected typo: services.PaymentService

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final StoredPaymentMethodRepository storedPaymentMethodRepository;
    private final PaymentMapper paymentMapper;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    private <T> void logKafkaSendAttempt(CompletableFuture<SendResult<String, T>> future, String eventName, String eventKey) {
        log.debug("Submitted {} to Kafka for key {}. Awaiting async result...", eventName, eventKey);
        future.whenComplete((sendResult, exception) -> {
            if (exception == null) {
                log.info("Successfully sent {} to topic {} for key {}: offset {}, partition {}",
                        eventName, PAYMENT_EVENTS_TOPIC, eventKey, // Assuming PAYMENT_EVENTS_TOPIC is universal for these
                        sendResult.getRecordMetadata().offset(), sendResult.getRecordMetadata().partition());
            } else {
                log.error("Failed to send {} to topic {} for key {}: {}",
                        eventName, PAYMENT_EVENTS_TOPIC, eventKey, exception.getMessage(), exception);
            }
        });
    }
    
    private Map<String, Object> deserializeMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Could not deserialize metadata: {}. Returning empty map.", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request) {
        log.info("Initiating payment for user ID: {}, reference ID: {}, type: {}, amount: {} {}",
                request.userId(), request.referenceId(), request.referenceType(), request.amount(), request.currency());

        PaymentTransaction transaction = PaymentTransaction.builder()
                .userId(request.userId())
                .referenceId(request.referenceId())
                .referenceType(request.referenceType())
                .amount(request.amount())
                .currency(request.currency())
                .paymentGateway(request.paymentGateway())
                .status(PaymentTransactionStatus.PENDING_CREATION)
                .description(request.description())
                .initiatedAt(Instant.now()) // This will be set by @CreatedDate if auditing is on
                .build();
        try {
            if (request.metadata() != null && !request.metadata().isEmpty()) {
                transaction.setMetadata(objectMapper.writeValueAsString(request.metadata()));
            }
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize metadata for payment initiation: {}", e.getMessage());
        }

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);
        log.info("PaymentTransaction {} created with status PENDING_CREATION.", savedTransaction.getId());

        PaymentInitiatedPayload initiatedPayload = new PaymentInitiatedPayload(
                savedTransaction.getId(), savedTransaction.getUserId(), savedTransaction.getReferenceId(),
                savedTransaction.getReferenceType(), savedTransaction.getAmount(), savedTransaction.getCurrency(),
                savedTransaction.getPaymentGateway(), savedTransaction.getStatus(), savedTransaction.getDescription(),
                deserializeMetadata(savedTransaction.getMetadata()), // Deserialize back for payload if needed as Map
                savedTransaction.getInitiatedAt()
        );
        CompletableFuture<SendResult<String, Object>> initiatedFuture = 
            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, savedTransaction.getId(), initiatedPayload);
        logKafkaSendAttempt(initiatedFuture, "PaymentInitiatedEvent", savedTransaction.getId());

        String clientSecretForFrontend = null;
        PaymentTransaction currentTransactionState = savedTransaction; // Use a new variable for modifications

        try {
            switch (request.paymentGateway()) {
                case STRIPE:
                    log.info("STRIPE: Mocking Stripe payment intent creation/confirmation for Tx ID {}.", currentTransactionState.getId());
                    currentTransactionState.setStatus(PaymentTransactionStatus.PENDING_GATEWAY_ACTION); // Or REQUIRES_CLIENT_ACTION
                    currentTransactionState.setGatewayPaymentIntentId("mock_pi_" + UUID.randomUUID().toString());
                    clientSecretForFrontend = currentTransactionState.getGatewayPaymentIntentId() + "_secret_mock_" + UUID.randomUUID().toString();
                    break;
                case PAYPAL:
                    log.info("PAYPAL: Mocking PayPal order creation for Tx ID {}.", currentTransactionState.getId());
                    currentTransactionState.setStatus(PaymentTransactionStatus.PENDING_GATEWAY_ACTION);
                    break;
                // Add MOCK gateway type for testing simple success/failure without client secret
                case MOCK:
                     log.info("MOCK_GATEWAY: Simulating direct success for Tx ID {}.", currentTransactionState.getId());
                     currentTransactionState.setStatus(PaymentTransactionStatus.SUCCEEDED);
                     currentTransactionState.setGatewayTransactionId("mock_tx_succeeded_" + UUID.randomUUID().toString());
                     currentTransactionState.setProcessedAt(Instant.now());
                     currentTransactionState.setPaymentMethodDetails("Mocked Successful Payment");
                     currentTransactionState.setPaymentMethodType(PaymentMethodType.CARD); // Example
                    break;
                default:
                    log.error("Unsupported payment gateway: {}", request.paymentGateway());
                    currentTransactionState.setStatus(PaymentTransactionStatus.FAILED);
                    currentTransactionState.setErrorMessage("Unsupported payment gateway: " + request.paymentGateway());
                    currentTransactionState = paymentTransactionRepository.save(currentTransactionState); // Save before publishing fail
                    publishPaymentFailedEventHelper(currentTransactionState, "Unsupported payment gateway");
                    throw new PaymentProcessingException("Unsupported payment gateway: " + request.paymentGateway());
            }
            currentTransactionState = paymentTransactionRepository.save(currentTransactionState); // Save status changes from switch

            if (clientSecretForFrontend != null &&
                (currentTransactionState.getStatus() == PaymentTransactionStatus.PENDING_GATEWAY_ACTION ||
                 currentTransactionState.getStatus() == PaymentTransactionStatus.REQUIRES_CLIENT_ACTION)) { // Added REQUIRES_CLIENT_ACTION
                PaymentRequiresActionPayload requiresActionPayload = new PaymentRequiresActionPayload(
                        currentTransactionState.getId(), currentTransactionState.getUserId(), currentTransactionState.getReferenceId(),
                        currentTransactionState.getReferenceType(), currentTransactionState.getAmount(), currentTransactionState.getCurrency(),
                        currentTransactionState.getPaymentGateway(), currentTransactionState.getGatewayPaymentIntentId(),
                        clientSecretForFrontend, deserializeMetadata(currentTransactionState.getMetadata()), Instant.now()
                );
                CompletableFuture<SendResult<String, Object>> requiresActionFuture =
                    kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, currentTransactionState.getId(), requiresActionPayload);
                logKafkaSendAttempt(requiresActionFuture, "PaymentRequiresActionEvent", currentTransactionState.getId());
            } else if (currentTransactionState.getStatus() == PaymentTransactionStatus.SUCCEEDED) {
                // If mocked success happened directly in this method
                publishPaymentSucceededEventHelper(currentTransactionState);
            }

        } catch (PaymentProcessingException ppe) {
            throw ppe; // Re-throw if already handled and event published
        } catch (Exception e) {
            log.error("Payment gateway interaction failed for transaction {}: {}", currentTransactionState.getId(), e.getMessage(), e);
            currentTransactionState.setStatus(PaymentTransactionStatus.FAILED);
            currentTransactionState.setErrorMessage("Gateway error: " + e.getMessage());
            currentTransactionState = paymentTransactionRepository.save(currentTransactionState);
            publishPaymentFailedEventHelper(currentTransactionState, "Gateway interaction failed");
            throw new PaymentProcessingException("Gateway interaction failed: " + e.getMessage(), e);
        }
        
        PaymentResponse response = paymentMapper.toPaymentResponse(currentTransactionState);
        // If you add clientSecret to PaymentResponse DTO:
        // if (clientSecretForFrontend != null) {
        //    response = response.withClientSecret(clientSecretForFrontend); // Assuming a wither method
        // }
        log.debug("Returning payment response for Tx ID {}: {}", currentTransactionState.getId(), response);
        return response;
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(PaymentStatusUpdateRequest request) {
        log.info("Updating payment status for transaction ID: {} to {}", request.paymentTransactionId(), request.newStatus());
        PaymentTransaction transaction = paymentTransactionRepository.findById(request.paymentTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction not found with ID: " + request.paymentTransactionId()));

        // Idempotency: if status is already the target status, or if it's a final success/fail state.
        if (transaction.getStatus() == request.newStatus()) {
            log.warn("PaymentTransaction {} already in status {}. No update performed.", transaction.getId(), request.newStatus());
            return paymentMapper.toPaymentResponse(transaction);
        }
        if (List.of(PaymentTransactionStatus.SUCCEEDED, PaymentTransactionStatus.FAILED, PaymentTransactionStatus.REFUNDED).contains(transaction.getStatus()) &&
            transaction.getStatus() != PaymentTransactionStatus.REFUND_REQUESTED && /* allow moving from REFUND_REQUESTED */
            transaction.getStatus() != PaymentTransactionStatus.REFUND_PROCESSING) {
            log.warn("PaymentTransaction {} is in a final state {} and cannot be updated to {}. No update performed.",
                     transaction.getId(), transaction.getStatus(), request.newStatus());
            return paymentMapper.toPaymentResponse(transaction);
        }


        transaction.setStatus(request.newStatus());
        if (request.actualGatewayTransactionId() != null) {
            transaction.setGatewayTransactionId(request.actualGatewayTransactionId());
        }
        if (request.paymentMethodDetails() != null) {
            transaction.setPaymentMethodDetails(request.paymentMethodDetails());
        }
        if (request.paymentMethodType() != null) {
            transaction.setPaymentMethodType(request.paymentMethodType());
        }

        Instant eventTimestamp = Instant.now();
        if (request.newStatus() == PaymentTransactionStatus.SUCCEEDED) {
            transaction.setProcessedAt(eventTimestamp);
            transaction.setErrorMessage(null);
            transaction.setGatewayErrorCode(null);
        } else if (request.newStatus() == PaymentTransactionStatus.FAILED) {
            transaction.setErrorMessage(request.errorMessage());
            transaction.setGatewayErrorCode(request.gatewayErrorCode());
            transaction.setProcessedAt(eventTimestamp); // Mark as processed even if failed
        }

        if (request.gatewayEventData() != null && !request.gatewayEventData().isEmpty()) {
            try {
                Map<String, Object> metadataMap = deserializeMetadata(transaction.getMetadata());
                metadataMap.put("lastGatewayEvent_" + eventTimestamp.toEpochMilli(), request.gatewayEventData());
                transaction.setMetadata(objectMapper.writeValueAsString(metadataMap));
            } catch (JsonProcessingException e) {
                log.warn("Could not update metadata with gateway event data for transaction {}: {}", transaction.getId(), e.getMessage());
            }
        }

        PaymentTransaction updatedTransaction = paymentTransactionRepository.save(transaction);
        log.info("PaymentTransaction {} status updated to {}.", updatedTransaction.getId(), updatedTransaction.getStatus());

        if (updatedTransaction.getStatus() == PaymentTransactionStatus.SUCCEEDED) {
            publishPaymentSucceededEventHelper(updatedTransaction);
        } else if (updatedTransaction.getStatus() == PaymentTransactionStatus.FAILED) {
            publishPaymentFailedEventHelper(updatedTransaction, request.errorMessage() != null ? request.errorMessage() : "Payment failed via status update.");
        }
        // Add other event publications for other statuses if needed

        return paymentMapper.toPaymentResponse(updatedTransaction);
    }

    private void publishPaymentSucceededEventHelper(PaymentTransaction transaction) {
        PaymentSucceededPayload payload = new PaymentSucceededPayload(
                transaction.getId(), transaction.getUserId(), transaction.getReferenceId(),
                transaction.getReferenceType(), transaction.getAmount(), transaction.getCurrency(),
                transaction.getPaymentGateway(), transaction.getGatewayTransactionId(),
                transaction.getGatewayPaymentIntentId(), transaction.getPaymentMethodType(),
                transaction.getPaymentMethodDetails(), deserializeMetadata(transaction.getMetadata()),
                transaction.getProcessedAt() != null ? transaction.getProcessedAt() : Instant.now() // Ensure processedAt is set
        );
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, transaction.getId(), payload);
        logKafkaSendAttempt(future, "PaymentSucceededEvent", transaction.getId());
    }

    private void publishPaymentFailedEventHelper(PaymentTransaction transaction, String defaultReason) {
        PaymentFailedPayload payload = new PaymentFailedPayload(
                transaction.getId(), transaction.getUserId(), transaction.getReferenceId(),
                transaction.getReferenceType(), transaction.getAmount(), transaction.getCurrency(),
                transaction.getPaymentGateway(), transaction.getGatewayPaymentIntentId(),
                transaction.getErrorMessage() != null ? transaction.getErrorMessage() : defaultReason,
                transaction.getGatewayErrorCode(), deserializeMetadata(transaction.getMetadata()),
                transaction.getProcessedAt() != null ? transaction.getProcessedAt() : Instant.now() // Time of failure processing
        );
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, transaction.getId(), payload);
        logKafkaSendAttempt(future, "PaymentFailedEvent", transaction.getId());
    }

    @Override
    @Transactional
    public StoredPaymentMethodResponse addStoredPaymentMethod(String userId, AddStoredPaymentMethodRequest request) {
        log.info("Attempting to add stored payment method for user ID: {} via gateway: {}", userId, request.paymentGateway());

        String gatewayCustomerId = request.gatewayCustomerId();
        String persistentGatewayPaymentMethodId;
        // --- MOCK GATEWAY INTERACTION for StoredPaymentMethod ---
        switch (request.paymentGateway()) {
            case STRIPE:
                log.warn("STRIPE: Mocking creating/attaching payment method token {} to customer {}.",
                         request.gatewayPaymentMethodToken(), gatewayCustomerId);
                if (gatewayCustomerId == null || gatewayCustomerId.isBlank()) gatewayCustomerId = "mock_cus_" + UUID.randomUUID().toString();
                persistentGatewayPaymentMethodId = "mock_stripe_pm_" + UUID.randomUUID().toString(); // This would come from Stripe
                break;
            case PAYPAL:
                log.warn("PAYPAL: Mocking processing PayPal payment method token {}.", request.gatewayPaymentMethodToken());
                if (gatewayCustomerId == null || gatewayCustomerId.isBlank()) gatewayCustomerId = "mock_paypal_payer_" + UUID.randomUUID().toString();
                persistentGatewayPaymentMethodId = "mock_paypal_token_" + UUID.randomUUID().toString();
                break;
            default:
                throw new PaymentProcessingException("Gateway not supported for storing payment methods: " + request.paymentGateway());
        }

        if (persistentGatewayPaymentMethodId == null) { // Should be caught by default in switch
            throw new PaymentProcessingException("Failed to obtain a persistent gateway payment method ID.");
        }

        StoredPaymentMethod oldDefaultSpm = null;
        if (request.isDefault()) {
            // Find existing default for this user and gateway, and unset it
            oldDefaultSpm = storedPaymentMethodRepository
                .findByUserIdAndPaymentGatewayAndIsDefaultTrueAndStatus(
                    userId, request.paymentGateway(), StoredPaymentMethodStatus.ACTIVE
                ).orElse(null);
            if (oldDefaultSpm != null) {
                log.debug("Unsetting default flag for existing default SPM ID: {}", oldDefaultSpm.getId());
                oldDefaultSpm.setDefault(false);
                storedPaymentMethodRepository.save(oldDefaultSpm); // Save the change to the old default
            }
        }

        StoredPaymentMethod spm = StoredPaymentMethod.builder()
                .userId(userId)
                .paymentGateway(request.paymentGateway())
                .gatewayCustomerId(gatewayCustomerId)
                .gatewayPaymentMethodId(persistentGatewayPaymentMethodId)
                .type(request.type())
                .brand(request.brand())
                .last4(request.last4())
                .expiryMonth(request.expiryMonth())
                .expiryYear(request.expiryYear())
                .isDefault(request.isDefault())
                .status(StoredPaymentMethodStatus.ACTIVE)
                // .addedAt will be set by @CreatedDate
                .build();
        try {
            if (request.billingDetails() != null && !request.billingDetails().isEmpty()) {
                spm.setBillingDetailsSnapshot(objectMapper.writeValueAsString(request.billingDetails()));
            }
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize billing details for stored payment method: {}", e.getMessage());
        }

        StoredPaymentMethod savedSpm = storedPaymentMethodRepository.save(spm);
        log.info("Stored payment method {} created for user {}.", savedSpm.getId(), userId);

        StoredPaymentMethodAddedPayload addedPayload = new StoredPaymentMethodAddedPayload(
                savedSpm.getId(), savedSpm.getUserId(), savedSpm.getPaymentGateway(),
                savedSpm.getGatewayCustomerId(), savedSpm.getGatewayPaymentMethodId(),
                savedSpm.getType(), savedSpm.getBrand(), savedSpm.getLast4(),
                savedSpm.getExpiryMonth(), savedSpm.getExpiryYear(), savedSpm.isDefault(),
                savedSpm.getStatus() // Use entity's @CreatedDate field
        );
        CompletableFuture<SendResult<String, Object>> addFuture =
            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, savedSpm.getUserId(), addedPayload); // Key by userId or spm.id
        logKafkaSendAttempt(addFuture, "StoredPaymentMethodAddedEvent", savedSpm.getUserId());

        // If this new SPM became default and there was an old default, or if no old default and this is new default
        if (savedSpm.isDefault() && (oldDefaultSpm == null || !oldDefaultSpm.getId().equals(savedSpm.getId()))) {
             publishStoredPaymentMethodDefaultChangedEventHelper(savedSpm, oldDefaultSpm, userId, request.paymentGateway());
        }

        return paymentMapper.toStoredPaymentMethodResponse(savedSpm);
    }

    @Override
    @Transactional
    public void deleteStoredPaymentMethod(String userId, String storedPaymentMethodId) {
        log.info("User {} attempting to delete stored payment method ID: {}", userId, storedPaymentMethodId);
        StoredPaymentMethod spm = storedPaymentMethodRepository.findByIdAndUserId(storedPaymentMethodId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("StoredPaymentMethod not found with ID " + storedPaymentMethodId + " for user " + userId));

        if (spm.getStatus() == StoredPaymentMethodStatus.REMOVED) {
            log.warn("Stored payment method {} already removed for user {}.", storedPaymentMethodId, userId);
            return; // Idempotent
        }

        // MOCK: Call gateway to detach/delete the payment method
        log.warn("{}: Mocking detachment of payment method {}.", spm.getPaymentGateway(), spm.getGatewayPaymentMethodId());
        // In a real scenario, if gateway detachment fails, you might not proceed or handle differently.

        boolean wasDefault = spm.isDefault();
        spm.setStatus(StoredPaymentMethodStatus.REMOVED);
        spm.setDefault(false); // A removed method cannot be default
        StoredPaymentMethod savedSpm = storedPaymentMethodRepository.save(spm); // Or use .delete(spm) if you prefer hard deletes
        log.info("Stored payment method {} marked as REMOVED for user {}.", storedPaymentMethodId, userId);

        StoredPaymentMethodRemovedPayload removedPayload = new StoredPaymentMethodRemovedPayload(
                savedSpm.getId(), savedSpm.getUserId(), savedSpm.getPaymentGateway(),
                savedSpm.getGatewayPaymentMethodId(), savedSpm.getUpdatedAt()
        );
        CompletableFuture<SendResult<String, Object>> removeFuture = 
            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, savedSpm.getUserId(), removedPayload); // Key by userId or spm.id
        logKafkaSendAttempt(removeFuture, "StoredPaymentMethodRemovedEvent", savedSpm.getUserId());

        if (wasDefault) {
            // If the deleted one was default, publish that no SPM is default now (unless another logic picks a new default)
            publishStoredPaymentMethodDefaultChangedEventHelper(null, savedSpm, userId, savedSpm.getPaymentGateway());
        }
    }

    @Override
    @Transactional
    public StoredPaymentMethodResponse setStoredPaymentMethodAsDefault(String userId, String storedPaymentMethodId) {
        log.info("User {} attempting to set stored payment method ID: {} as default.", userId, storedPaymentMethodId);
        StoredPaymentMethod spmToSetDefault = storedPaymentMethodRepository.findByIdAndUserId(storedPaymentMethodId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("StoredPaymentMethod not found with ID " + storedPaymentMethodId + " for user " + userId));

        if (spmToSetDefault.isDefault() && spmToSetDefault.getStatus() == StoredPaymentMethodStatus.ACTIVE) {
            log.warn("Stored payment method {} is already default and active for user {}. No change.", storedPaymentMethodId, userId);
            return paymentMapper.toStoredPaymentMethodResponse(spmToSetDefault);
        }
        if (spmToSetDefault.getStatus() != StoredPaymentMethodStatus.ACTIVE) {
            throw new PaymentProcessingException("Payment method " + storedPaymentMethodId + " is not active and cannot be set as default.");
        }

        // Find current default for this user and gateway (if any)
        StoredPaymentMethod oldDefaultSpm = storedPaymentMethodRepository
            .findByUserIdAndPaymentGatewayAndIsDefaultTrueAndStatus(
                userId, spmToSetDefault.getPaymentGateway(), StoredPaymentMethodStatus.ACTIVE
            ).orElse(null);

        // Unset old default if it exists and is different from the one being set as default
        if (oldDefaultSpm != null && !oldDefaultSpm.getId().equals(spmToSetDefault.getId())) {
            log.debug("Unsetting default flag for existing default SPM ID: {}", oldDefaultSpm.getId());
            oldDefaultSpm.setDefault(false);
            storedPaymentMethodRepository.save(oldDefaultSpm);
        }

        spmToSetDefault.setDefault(true);
        StoredPaymentMethod updatedSpm = storedPaymentMethodRepository.save(spmToSetDefault);
        log.info("Stored payment method {} set as default for user {}.", storedPaymentMethodId, userId);

        // Publish event only if there was an actual change in which SPM is default
        if (oldDefaultSpm == null || !oldDefaultSpm.getId().equals(updatedSpm.getId())) {
             publishStoredPaymentMethodDefaultChangedEventHelper(updatedSpm, oldDefaultSpm, userId, updatedSpm.getPaymentGateway());
        }
        
        return paymentMapper.toStoredPaymentMethodResponse(updatedSpm);
    }

    private void publishStoredPaymentMethodDefaultChangedEventHelper(StoredPaymentMethod newDefaultSpm, StoredPaymentMethod oldDefaultSpmIfAny, String userId, PaymentGatewayType gateway) {
        // Avoid publishing if no effective change to the default status occurred
        if (newDefaultSpm != null && oldDefaultSpmIfAny != null && newDefaultSpm.getId().equals(oldDefaultSpmIfAny.getId()) && newDefaultSpm.isDefault()) {
            log.debug("Default SPM change event skipped: new default is the same as old default and already marked default.");
            return;
        }
        if (newDefaultSpm == null && oldDefaultSpmIfAny == null) {
            log.debug("Default SPM change event skipped: no new default and no old default to unset.");
            return;
        }


        StoredPaymentMethodDefaultChangedPayload defaultChangedPayload = new StoredPaymentMethodDefaultChangedPayload(
                newDefaultSpm != null ? newDefaultSpm.getId() : null,
                oldDefaultSpmIfAny != null ? oldDefaultSpmIfAny.getId() : null,
                userId,
                gateway,
                Instant.now()
        );
        // Keying by userId makes sense as it's a user-level change for a gateway
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, userId, defaultChangedPayload);
        logKafkaSendAttempt(future, "StoredPaymentMethodDefaultChangedEvent", userId);
    }

    // --- Read-only methods ---
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentTransactionById(String paymentTransactionId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(paymentTransactionId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction not found with ID: " + paymentTransactionId));
        return paymentMapper.toPaymentResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentTransactionsByUserId(String userId) {
        return paymentTransactionRepository.findByUserIdOrderByInitiatedAtDesc(userId).stream()
                .map(paymentMapper::toPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentTransactionsByReferenceId(String referenceId) {
         return paymentTransactionRepository.findByReferenceIdOrderByInitiatedAtDesc(referenceId).stream()
                .map(paymentMapper::toPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoredPaymentMethodResponse> listStoredPaymentMethods(String userId, PaymentGatewayType gateway) {
        List<StoredPaymentMethod> spms;
        if (gateway != null) {
            spms = storedPaymentMethodRepository.findByUserIdAndPaymentGatewayAndStatusNot(userId, gateway, StoredPaymentMethodStatus.REMOVED);
        } else {
            spms = storedPaymentMethodRepository.findByUserIdAndStatusNot(userId, StoredPaymentMethodStatus.REMOVED);
        }
        return spms.stream()
                .map(paymentMapper::toStoredPaymentMethodResponse)
                .collect(Collectors.toList());
    }
}