package com.appverse.order_service.model;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.appverse.order_service.enums.FulfillmentStatus;
import com.appverse.order_service.enums.OrderItemType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID; // Still needed for generation

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {

    @Id
    @Column(length = 36) // Standard UUID string length
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_order_id", nullable = false)
    private CustomerOrder customerOrder;

    // ... other fields remain the same ...
    @Column(name = "application_id", nullable = false, length = 255)
    private String applicationId;

    @Column(name = "application_name", nullable = false, length = 255)
    private String applicationName;

    @Column(name = "application_version", length = 50)
    private String applicationVersion;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalPrice;

    @Column(nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private OrderItemType itemType;

    @Column(name = "subscription_plan_id", length = 255)
    private String subscriptionPlanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false, length = 50)
    private FulfillmentStatus fulfillmentStatus;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @PrePersist
    protected void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}