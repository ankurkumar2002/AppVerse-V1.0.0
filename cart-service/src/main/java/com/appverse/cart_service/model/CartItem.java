package com.appverse.cart_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID; 

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) 
    private UUID id; 

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false) 
    private Cart cart;

    @Column(name = "application_id", nullable = false, length = 255) 
    private String applicationId;   

    @Column(name = "application_name", nullable = false, length = 255)
    private String applicationName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4) 
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 10) 
    private String currency;

    @Column(name = "is_free", nullable = false)
    private boolean isFree;

    @Column(name = "thumbnail_url", length = 512) 
    private String thumbnailUrl;


    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = Instant.now();
        }
    }
}