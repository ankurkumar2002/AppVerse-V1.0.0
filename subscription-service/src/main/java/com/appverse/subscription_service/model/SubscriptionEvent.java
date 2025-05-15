// === In Subscription Service Project ===
package com.appverse.subscription_service.model;

import com.appverse.subscription_service.enums.SubscriptionEventType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate; // Not strictly needed if using eventTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // Only if using @CreatedDate

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscription_events", indexes = {
    @Index(name = "idx_subevent_usersub_id", columnList = "userSubscriptionId"),
    @Index(name = "idx_subevent_event_type", columnList = "eventType")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// @EntityListeners(AuditingEntityListener.class) // Only if using @CreatedDate/@LastModifiedDate
public class SubscriptionEvent {

    @Id
    @Column(length = 36) // For UUID string
    private String id;

    @Column(name = "user_subscription_id", nullable = false, length = 36)
    private String userSubscriptionId; // FK to UserSubscription.id

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private SubscriptionEventType eventType;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Lob
    @Column(columnDefinition = "TEXT") // For JSON string or longer text
    private String details; // e.g., old status, new status, payment ID, plan change info

    @Column(name = "triggered_by", length = 100) // USER, SYSTEM_RENEWAL, ADMIN, GATEWAY_WEBHOOK
    private String triggeredBy;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.eventTimestamp == null) {
            this.eventTimestamp = Instant.now();
        }
    }
}