// === In Payment Service Project ===
package com.appverse.payment_service.services.serviceImpl;

import com.appverse.payment_service.dto.*;
import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentTransactionStatus;
import com.appverse.payment_service.enums.StoredPaymentMethodStatus;
import com.appverse.payment_service.exception.PaymentProcessingException;
import com.appverse.payment_service.exception.ResourceNotFoundException;
import com.appverse.payment_service.mapper.PaymentMapper; // You'll need to create this
import com.appverse.payment_service.model.PaymentTransaction;
import com.appverse.payment_service.model.StoredPaymentMethod;
import com.appverse.payment_service.repository.PaymentTransactionRepository;
import com.appverse.payment_service.repository.StoredPaymentMethodRepository;
import com.appverse.payment_service.services.PaymentService;
// import com.stripe.Stripe; // Example for Stripe
// import com.stripe.model.PaymentIntent;
// import com.stripe.param.PaymentIntentCreateParams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // For Stripe API key
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct; // For Stripe API key init
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final StoredPaymentMethodRepository storedPaymentMethodRepository;
    private final PaymentMapper paymentMapper;
    private final ObjectMapper objectMapper; // For serializing/deserializing metadata

    // @Value("${stripe.api.secret-key}") // Example: Inject Stripe secret key
    // private String stripeSecretKey;

    // @PostConstruct
    // public void init() {
    //     Stripe.apiKey = stripeSecretKey;
    // }

    @Override
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request) {
        log.info("Initiating payment for reference ID: {}, type: {}, amount: {} {}",
                request.referenceId(), request.referenceType(), request.amount(), request.currency());

        // 1. Create PaymentTransaction record in PENDING_CREATION or PENDING_GATEWAY_ACTION status
        PaymentTransaction transaction = PaymentTransaction.builder()
                .userId(request.userId())
                .referenceId(request.referenceId())
                .referenceType(request.referenceType())
                .amount(request.amount())
                .currency(request.currency())
                .paymentGateway(request.paymentGateway())
                .status(PaymentTransactionStatus.PENDING_CREATION) // Initial status
                .description(request.description())
                .initiatedAt(Instant.now())
                .build();
        try {
            if (request.metadata() != null) {
                transaction.setMetadata(objectMapper.writeValueAsString(request.metadata()));
            }
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize metadata for payment initiation: {}", e.getMessage());
            // Decide if this is a critical failure or just a warning
        }

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);
        log.info("PaymentTransaction {} created with status PENDING_CREATION.", savedTransaction.getId());

        // 2. Interact with the chosen Payment Gateway
        //    This part is highly dependent on the gateway (Stripe, PayPal, etc.)
        //    You would use their SDK here.
        try {
            switch (request.paymentGateway()) {
                case STRIPE:
                    // EXAMPLE for Stripe PaymentIntent:
                    // PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    //     .setAmount(request.amount().multiply(new BigDecimal("100")).longValue()) // Amount in cents
                    //     .setCurrency(request.currency().toLowerCase())
                    //     .setPaymentMethod(request.paymentMethodToken()) // if provided for one-off
                    //     .setCustomer(request.customerId()) // if customer and saved method are used
                    //     .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL) // or AUTOMATIC
                    //     .setConfirm(true) // or false if frontend confirms
                    //     .setDescription(request.description())
                    //     .putMetadata("internal_payment_id", savedTransaction.getId())
                    //     .putMetadata("reference_id", request.referenceId())
                    //     .build();
                    // PaymentIntent paymentIntent = PaymentIntent.create(params);
                    // savedTransaction.setGatewayPaymentIntentId(paymentIntent.getId());
                    // savedTransaction.setStatus(determineStatusFromStripeIntent(paymentIntent)); // map Stripe status
                    // // If successful immediately (e.g. card, confirm=true)
                    // if ("succeeded".equals(paymentIntent.getStatus())) {
                    //    savedTransaction.setGatewayTransactionId(paymentIntent.getLatestCharge()); // or from charge object
                    //    savedTransaction.setProcessedAt(Instant.now());
                    // }
                    // // If requires action: return paymentIntent.getClientSecret() to frontend in PaymentResponse
                    log.info("STRIPE: Placeholder for Stripe payment intent creation/confirmation. PaymentIntent ID would be set here.");
                    savedTransaction.setStatus(PaymentTransactionStatus.PENDING_GATEWAY_ACTION); // Example update
                    break;
                case PAYPAL:
                    // Placeholder for PayPal SDK interaction (e.g., create order, get approval URL)
                    log.info("PAYPAL: Placeholder for PayPal order creation. Approval URL would be generated here.");
                    savedTransaction.setStatus(PaymentTransactionStatus.PENDING_GATEWAY_ACTION); // Example update
                    break;
                default:
                    log.error("Unsupported payment gateway: {}", request.paymentGateway());
                    throw new PaymentProcessingException("Unsupported payment gateway: " + request.paymentGateway());
            }
            // Save again to update gateway IDs and status
            savedTransaction = paymentTransactionRepository.save(savedTransaction);

        } catch (Exception e) { // Catch specific gateway exceptions
            log.error("Payment gateway interaction failed for transaction {}: {}", savedTransaction.getId(), e.getMessage(), e);
            savedTransaction.setStatus(PaymentTransactionStatus.FAILED);
            savedTransaction.setErrorMessage("Gateway error: " + e.getMessage());
            paymentTransactionRepository.save(savedTransaction);
            // Re-throw or wrap in a custom exception
            throw new PaymentProcessingException("Gateway interaction failed: " + e.getMessage(), e);
        }

        PaymentResponse response = paymentMapper.toPaymentResponse(savedTransaction);
        // If Stripe/gateway needs client secret for frontend confirmation, add it to response
        // if (paymentIntent != null && "requires_action".equals(paymentIntent.getStatus())) {
        //     response = response.withClientSecret(paymentIntent.getClientSecret()); // Assuming PaymentResponse has withClientSecret
        // }
        return response;
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(PaymentStatusUpdateRequest request) {
        log.info("Updating payment status for transaction ID: {} to {}", request.paymentTransactionId(), request.newStatus());
        PaymentTransaction transaction = paymentTransactionRepository.findById(request.paymentTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction not found with ID: " + request.paymentTransactionId()));

        // Idempotency checks & status transition validation would go here
        // For example, don't update if already SUCCEEDED or FAILED to a PENDING state.

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
        if (request.newStatus() == PaymentTransactionStatus.SUCCEEDED) {
            transaction.setProcessedAt(Instant.now());
            transaction.setErrorMessage(null); // Clear previous errors
            transaction.setGatewayErrorCode(null);
        } else if (request.newStatus() == PaymentTransactionStatus.FAILED) {
            transaction.setErrorMessage(request.errorMessage());
            transaction.setGatewayErrorCode(request.gatewayErrorCode());
        }

        // Store raw gateway event data if provided
        if (request.gatewayEventData() != null && !request.gatewayEventData().isEmpty()) {
            try {
                String currentMetadata = transaction.getMetadata();
                Map<String, Object> metadataMap = currentMetadata != null ?
                        objectMapper.readValue(currentMetadata, Map.class) :
                        new java.util.HashMap<>();
                metadataMap.put("lastGatewayEvent", request.gatewayEventData());
                transaction.setMetadata(objectMapper.writeValueAsString(metadataMap));
            } catch (JsonProcessingException e) {
                log.warn("Could not update metadata with gateway event data for transaction {}: {}", transaction.getId(), e.getMessage());
            }
        }


        PaymentTransaction updatedTransaction = paymentTransactionRepository.save(transaction);
        log.info("PaymentTransaction {} status updated to {}.", updatedTransaction.getId(), updatedTransaction.getStatus());

        // TODO: Publish PaymentSucceededEvent or PaymentFailedEvent to a message queue
        // so other services like OrderService can react.
        // if (updatedTransaction.getStatus() == PaymentTransactionStatus.SUCCEEDED) {
        //    eventPublisher.publishPaymentSucceeded(paymentMapper.toPaymentResponse(updatedTransaction));
        // } else if (updatedTransaction.getStatus() == PaymentTransactionStatus.FAILED) {
        //    eventPublisher.publishPaymentFailed(paymentMapper.toPaymentResponse(updatedTransaction));
        // }

        return paymentMapper.toPaymentResponse(updatedTransaction);
    }

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


    // --- Stored Payment Methods ---

    @Override
    @Transactional
    public StoredPaymentMethodResponse addStoredPaymentMethod(String userId, AddStoredPaymentMethodRequest request) {
        log.info("Attempting to add stored payment method for user ID: {} via gateway: {}", userId, request.paymentGateway());

        // 1. Use the gateway's SDK to process the paymentMethodToken
        //    This usually involves creating/retrieving a Customer object at the gateway
        //    and then attaching the tokenized payment method to that customer.
        //    The gateway will return a persistent payment method ID (gatewayPaymentMethodId).

        String gatewayCustomerId = request.gatewayCustomerId(); // Or create/retrieve from gateway
        String persistentGatewayPaymentMethodId;

        switch (request.paymentGateway()) {
            case STRIPE:
                // Example:
                // com.stripe.model.PaymentMethod stripePM =
                //     com.stripe.model.PaymentMethod.retrieve(request.gatewayPaymentMethodToken());
                // if (gatewayCustomerId == null) {
                //    CustomerCreateParams customerParams = CustomerCreateParams.builder().setEmail(userEmailFromUserService).build();
                //    Customer stripeCustomer = Customer.create(customerParams);
                //    gatewayCustomerId = stripeCustomer.getId();
                // }
                // stripePM = stripePM.attach(PaymentMethodAttachParams.builder().setCustomer(gatewayCustomerId).build());
                // persistentGatewayPaymentMethodId = stripePM.getId();
                // // Extract brand, last4, expiry from stripePM.getCard() if type is CARD
                log.warn("STRIPE: Placeholder for creating/attaching payment method token {} to customer {}.",
                         request.gatewayPaymentMethodToken(), gatewayCustomerId);
                persistentGatewayPaymentMethodId = "mock_stripe_pm_" + UUID.randomUUID().toString(); // MOCK
                break;
            case PAYPAL:
                // PayPal might involve storing a billing agreement ID or similar token
                log.warn("PAYPAL: Placeholder for processing PayPal payment method token {}.", request.gatewayPaymentMethodToken());
                persistentGatewayPaymentMethodId = "mock_paypal_token_" + UUID.randomUUID().toString(); // MOCK
                break;
            default:
                throw new PaymentProcessingException("Gateway not supported for storing payment methods: " + request.paymentGateway());
        }

        if (persistentGatewayPaymentMethodId == null) {
            throw new PaymentProcessingException("Failed to obtain a persistent gateway payment method ID.");
        }

        // 2. If isDefault is true, ensure other methods for this user/gateway are not default
        if (request.isDefault()) {
            storedPaymentMethodRepository.clearDefaultForUserAndGateway(userId, request.paymentGateway());
        }

        // 3. Create and save StoredPaymentMethod entity
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
                .status(StoredPaymentMethodStatus.ACTIVE) // Assuming active after successful gateway interaction
                .build();
        try {
            if (request.billingDetails() != null) {
                spm.setBillingDetailsSnapshot(objectMapper.writeValueAsString(request.billingDetails()));
            }
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize billing details for stored payment method: {}", e.getMessage());
        }


        StoredPaymentMethod savedSpm = storedPaymentMethodRepository.save(spm);
        log.info("Stored payment method {} created for user {}.", savedSpm.getId(), userId);
        return paymentMapper.toStoredPaymentMethodResponse(savedSpm);
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

    @Override
    @Transactional
    public void deleteStoredPaymentMethod(String userId, String storedPaymentMethodId) {
        log.info("User {} attempting to delete stored payment method ID: {}", userId, storedPaymentMethodId);
        StoredPaymentMethod spm = storedPaymentMethodRepository.findByIdAndUserId(storedPaymentMethodId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("StoredPaymentMethod not found with ID " + storedPaymentMethodId + " for user " + userId));

        // 1. Call gateway to detach/delete the payment method
        // switch (spm.getPaymentGateway()) {
        //     case STRIPE:
        //         // com.stripe.model.PaymentMethod stripePM = com.stripe.model.PaymentMethod.retrieve(spm.getGatewayPaymentMethodId());
        //         // stripePM.detach();
        //         log.warn("STRIPE: Placeholder for detaching payment method {}.", spm.getGatewayPaymentMethodId());
        //         break;
        //     // Handle other gateways
        // }

        // 2. Mark as REMOVED in your DB (or actually delete)
        spm.setStatus(StoredPaymentMethodStatus.REMOVED);
        storedPaymentMethodRepository.save(spm); // Or .delete(spm)
        log.info("Stored payment method {} marked as REMOVED for user {}.", storedPaymentMethodId, userId);
    }

    @Override
    @Transactional
    public StoredPaymentMethodResponse setStoredPaymentMethodAsDefault(String userId, String storedPaymentMethodId) {
        log.info("User {} attempting to set stored payment method ID: {} as default.", userId, storedPaymentMethodId);
        StoredPaymentMethod spmToSetDefault = storedPaymentMethodRepository.findByIdAndUserId(storedPaymentMethodId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("StoredPaymentMethod not found with ID " + storedPaymentMethodId + " for user " + userId));

        if (spmToSetDefault.getStatus() != StoredPaymentMethodStatus.ACTIVE) {
            throw new PaymentProcessingException("Payment method " + storedPaymentMethodId + " is not active and cannot be set as default.");
        }

        storedPaymentMethodRepository.clearDefaultForUserAndGateway(userId, spmToSetDefault.getPaymentGateway());
        spmToSetDefault.setDefault(true);
        StoredPaymentMethod updatedSpm = storedPaymentMethodRepository.save(spmToSetDefault);
        log.info("Stored payment method {} set as default for user {}.", storedPaymentMethodId, userId);
        return paymentMapper.toStoredPaymentMethodResponse(updatedSpm);
    }
}