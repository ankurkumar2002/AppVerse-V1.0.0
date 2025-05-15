package com.appverse.cart_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID; // For a potential CartItem specific ID

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Or GenerationType.UUID if DB supports it well and you prefer UUIDs here
    private UUID id; // Primary key for the cart_items table itself

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false) // Foreign key to the Cart entity
    private Cart cart;

    @Column(name = "application_id", nullable = false, length = 255) // Assuming application IDs are strings (like UUIDs or custom codes)
    private String applicationId;   // Foreign key to Application in app-service

    @Column(name = "application_name", nullable = false, length = 255) // Denormalized
    private String applicationName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4) // Price per unit at time of adding
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 10) // e.g., "USD", "EUR"
    private String currency;

    @Column(name = "is_free", nullable = false)
    private boolean isFree;

    @Column(name = "thumbnail_url", length = 512) // Denormalized
    private String thumbnailUrl;

    // Optional: Could add developerId if needed for cart logic/display
    // @Column(name = "developer_id", length = 255)
    // private String developerId;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = Instant.now();
        }
    }
}