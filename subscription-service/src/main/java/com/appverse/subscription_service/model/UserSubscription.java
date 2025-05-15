// === In Subscription Service Project ===
package com.appverse.subscription_service.model;

import com.appverse.subscription_service.enums.UserSubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_subscriptions", indexes = {
    @Index(name = "idx_usersub_user_id", columnList = "userId"),
    @Index(name = "idx_usersub_plan_id", columnList = "subscriptionPlanId"),
    @Index(name = "idx_usersub_status", columnList = "status"),
    @Index(name = "idx_usersub_gateway_sub_id", columnList = "gatewaySubscriptionId", unique = true) // If gateway manages subscription
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserSubscription {

    @Id
    @Column(length = 36) // For UUID string
    private String id;

    @Column(nullable = false, length = 255)
    private String userId; // Keycloak User ID

    @Column(name = "subscription_plan_id", nullable = false, length = 36)
    private String subscriptionPlanId; // FK to SubscriptionPlan.id (conceptually)

    // If SubscriptionPlan is also an entity in this service, you can use @ManyToOne
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "subscription_plan_id", nullable = false)
    // private SubscriptionPlan plan;
    // For now, keeping it as String ID for simplicity, assuming plan details are fetched as needed.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserSubscriptionStatus status;

    @Column(nullable = false)
    private Instant startDate; // When the subscription (or trial) period began

    @Column // End of the entire subscription if it's non-renewing or cancelled
    private Instant endDate;

    @Column(name = "current_period_start_date", nullable = false)
    private Instant currentPeriodStartDate;

    @Column(name = "current_period_end_date", nullable = false)
    private Instant currentPeriodEndDate; // Next renewal/expiry date of current term

    @Column(name = "trial_end_date")
    private Instant trialEndDate;

    @Column(name = "cancelled_at")
    private Instant cancelledAt; // When cancellation was requested

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "auto_renew", nullable = false)
    @Builder.Default
    private boolean autoRenew = true;

    // ID of the StoredPaymentMethod in PaymentService to be used for renewals
    @Column(name = "stored_payment_method_id", length = 36)
    private String storedPaymentMethodId;

    // If the payment gateway manages the recurring subscription (e.g., Stripe Subscription ID sub_xxx)
    @Column(name = "gateway_subscription_id", length = 255)
    private String gatewaySubscriptionId;

    // ID of the last successful PaymentTransaction in PaymentService for this subscription
    @Column(name = "last_successful_payment_id", length = 36)
    private String lastSuccessfulPaymentId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

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