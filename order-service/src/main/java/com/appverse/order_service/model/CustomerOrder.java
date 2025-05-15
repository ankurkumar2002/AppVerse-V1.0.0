package com.appverse.order_service.model;




import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.appverse.order_service.enums.OrderStatus;
import com.appverse.order_service.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders") // "orders" might be a reserved keyword in some DBs
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CustomerOrder { // Renamed to CustomerOrder to avoid SQL keyword conflict

    @Id
    @Column(length = 36)  // Or GenerationType.UUID
    private String id;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId; // Keycloak User ID

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 50)
    private OrderStatus orderStatus;

    @Column(name = "order_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal orderTotal;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "payment_transaction_id", length = 255)
    private String paymentTransactionId; // From Payment Service

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50)
    private PaymentStatus paymentStatus; // Denormalized from Payment Service

    @Column(length = 500)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "customerOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // Helper methods for items
    public void addItem(OrderItem item) {
        items.add(item);
        item.setCustomerOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setCustomerOrder(null);
    }
}