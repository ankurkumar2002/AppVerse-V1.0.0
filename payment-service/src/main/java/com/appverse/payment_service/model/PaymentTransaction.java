package com.appverse.payment_service.model;

import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentReferenceType;
import com.appverse.payment_service.enums.PaymentTransactionStatus;
import com.appverse.payment_service.enums.PaymentMethodType; // If you want to store this detail

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_payment_tx_user_id", columnList = "userId"),
    @Index(name = "idx_payment_tx_reference_id_type", columnList = "referenceId, referenceType"),
    @Index(name = "idx_payment_tx_gateway_tx_id", columnList = "gatewayTransactionId"),
    @Index(name = "idx_payment_tx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PaymentTransaction {

    @Id
    @Column(length = 36) // For UUID string
    private String id;

    @Column(nullable = false, length = 255)
    private String userId; // Keycloak User ID of the customer/initiator

    @Column(length = 255)
    private String payerEmail; // Email of the person making the payment (optional)

    @Column(nullable = false, length = 255)
    private String referenceId; // e.g., Order ID, Subscription ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentReferenceType referenceType; // ORDER, SUBSCRIPTION_INITIAL, etc.

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency; // e.g., "USD"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentGatewayType paymentGateway; // STRIPE, PAYPAL, etc.

    @Column(length = 255, unique = true) // Gateway IDs are usually unique
    private String gatewayTransactionId; // ID from the gateway (e.g., Stripe Charge ID)

    @Column(length = 255)
    private String gatewayPaymentIntentId; // e.g., Stripe PaymentIntent ID

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PaymentMethodType paymentMethodType; // CARD, BANK_TRANSFER (from gateway)

    @Column(length = 255)
    private String paymentMethodDetails; // Masked details, e.g., "Visa ending in 4242"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentTransactionStatus status;

    @Column(length = 500)
    private String description; // Optional description of the payment

    @Lob // For potentially longer error messages
    private String errorMessage; // If payment failed

    @Column(length = 100)
    private String gatewayErrorCode; // Specific error code from gateway

    @Lob // For flexible metadata
    @Column(columnDefinition = "TEXT") // For MySQL, TEXT is suitable for JSON strings
    private String metadata; // JSON string for additional data

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant initiatedAt; // When this transaction was created in our system

    @Column
    private Instant processedAt; // When the gateway confirmed successful processing

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        // initiatedAt and updatedAt will be handled by @CreatedDate and @LastModifiedDate
    }
}