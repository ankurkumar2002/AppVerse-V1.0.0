package com.appverse.cart_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate; 
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener; 

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id"}, name = "uk_cart_user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) 
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) 
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, length = 255) 
    private String userId;  

    @Version
    private Long version;

    @OneToMany(
        mappedBy = "cart", 
        cascade = CascadeType.ALL,
        orphanRemoval = true,     
        fetch = FetchType.LAZY     
    )
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @CreatedDate 
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }
}