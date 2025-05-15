package com.appverse.cart_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate; // For JPA Auditing
import org.springframework.data.annotation.LastModifiedDate; // For JPA Auditing
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // For JPA Auditing

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id"}, name = "uk_cart_user_id") // Enforce one active cart per user
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) 
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Or GenerationType.UUID
    private UUID id; // Primary key for the carts table itself

    @Column(name = "user_id", nullable = false, unique = true, length = 255) // Assuming Keycloak user IDs are strings
    private String userId;  // Keycloak User ID (the 'sub' claim from JWT)

    // In a relational model, items are typically mapped via @OneToMany
    @OneToMany(
        mappedBy = "cart", // "cart" is the field in CartItem that owns the relationship
        cascade = CascadeType.ALL, // If a Cart is deleted, its CartItems are also deleted.
                                   // If a new CartItem is added to Cart's list and Cart is saved, CartItem is also saved.
        orphanRemoval = true,      // If a CartItem is removed from Cart's list and Cart is saved, CartItem is deleted from DB.
        fetch = FetchType.LAZY     // Load items only when explicitly accessed (e.g., cart.getItems())
    )
    @Builder.Default // Initialize the list
    private List<CartItem> items = new ArrayList<>();

    @CreatedDate // Handled by JPA Auditing
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate // Handled by JPA Auditing
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Optional: Cart Status
    // @Enumerated(EnumType.STRING)
    // @Column(name = "cart_status", length = 20)
    // private CartStatus status; // e.g., ACTIVE, COMPLETED, ABANDONED

    // Helper methods to manage items (maintains bi-directional consistency)
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }
}