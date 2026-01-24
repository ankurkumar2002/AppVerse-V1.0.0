package com.appverse.subscription_service.model;

import com.appverse.subscription_service.enums.SubscriptionEventType;
import com.appverse.subscription_service.event.payload.SubscriptionEventTrigger;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "subscription_events",
    indexes = {
        @Index(name = "idx_subevent_usersub_id", columnList = "user_subscription_id"),
        @Index(name = "idx_subevent_event_type", columnList = "event_type"),
        @Index(name = "idx_subevent_usersub_ts", columnList = "user_subscription_id, event_timestamp")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionEvent {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_subscription_id", nullable = false, length = 36)
    private String userSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private SubscriptionEventType eventType;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by", nullable = false, length = 30)
    private SubscriptionEventTrigger triggeredBy;

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
