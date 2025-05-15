// === In Payment Service Project ===
package com.appverse.payment_service.model;

import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.PaymentMethodType;
import com.appverse.payment_service.enums.StoredPaymentMethodStatus;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stored_payment_methods", indexes = {
    @Index(name = "idx_spm_user_id_gateway", columnList = "userId, paymentGateway"),
    @Index(name = "idx_spm_gateway_customer_id", columnList = "gatewayCustomerId"),
    @Index(name = "idx_spm_gateway_pm_id", columnList = "gatewayPaymentMethodId", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class StoredPaymentMethod {

    @Id
    @Column(length = 36) // For UUID string
    private String id;

    @Column(nullable = false, length = 255)
    private String userId; // Keycloak User ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentGatewayType paymentGateway; // e.g., STRIPE

    @Column(length = 255)
    private String gatewayCustomerId; // e.g., Stripe cus_xxx

    @Column(nullable = false, length = 255)
    private String gatewayPaymentMethodId; // e.g., Stripe pm_xxx or card_xxx

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentMethodType type; // CARD, BANK_ACCOUNT

    @Column(length = 50)
    private String brand; // e.g., "Visa"

    @Column(length = 4)
    private String last4; // Last 4 digits

    private Integer expiryMonth;
    private Integer expiryYear;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @Lob
    @Column(columnDefinition = "TEXT") // For JSON string
    private String billingDetailsSnapshot; // JSON snapshot of billing address

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StoredPaymentMethodStatus status = StoredPaymentMethodStatus.ACTIVE;

    private Instant expiresAt; // For cards, if provided by gateway

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant addedAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}