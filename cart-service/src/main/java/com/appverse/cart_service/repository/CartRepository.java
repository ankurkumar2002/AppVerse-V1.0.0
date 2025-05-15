package com.appverse.cart_service.repository;


import com.appverse.cart_service.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    /**
     * Finds a cart by the user's ID (Keycloak User ID).
     * Since userId is unique in the Cart entity, this should return at most one cart.
     *
     * @param userId The Keycloak User ID.
     * @return An Optional containing the Cart if found, or an empty Optional otherwise.
     */
    Optional<Cart> findByUserId(String userId);

}