// === In Subscription Service Project ===
package com.appverse.subscription_service.model;

import com.appverse.subscription_service.enums.SubscriptionPlanBillingInterval;
import com.appverse.subscription_service.enums.SubscriptionPlanStatus;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List; // For associatedApplicationIds or features
import java.util.UUID;

@Entity
@Table(name = "subscription_plans", indexes = {
    @Index(name = "idx_subplan_name", columnList = "name", unique = true), // Plan names should be unique
    @Index(name = "idx_subplan_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SubscriptionPlan {

    @Id
    @Column(length = 36) // For UUID string
    private String id; // Can be a UUID string or a human-readable unique slug

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price; // Recurring price for the interval

    @Column(nullable = false, length = 10)
    private String currency; // e.g., "USD"

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_interval", nullable = false, length = 20)
    private SubscriptionPlanBillingInterval billingInterval; // MONTHLY, YEARLY, etc.

    @Column(name = "billing_interval_count", nullable = false)
    @Builder.Default
    private int billingIntervalCount = 1; // e.g., 1 for MONTHLY means every month

    @Column(name = "trial_period_days")
    @Builder.Default
    private Integer trialPeriodDays = 0; // Duration of free trial in days

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubscriptionPlanStatus status = SubscriptionPlanStatus.INACTIVE; 

    @Column(name = "gateway_plan_price_id", length = 255)
    private String gatewayPlanPriceId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

        @Column(name = "application_id", nullable = false, length = 36) 
    private String applicationId;

    @Column(name = "developer_id", nullable = false, length = 255) 
    private String developerId;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString(); 
        }
    }
}