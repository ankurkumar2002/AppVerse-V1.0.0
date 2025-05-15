package com.appverse.cart_service.service;

import com.appverse.cart_service.dto.AddItemToCartRequest; // Assuming you have this DTO
import com.appverse.cart_service.dto.CartResponse;         // Assuming you have this DTO
import com.appverse.cart_service.dto.UpdateCartItemQuantityRequest; // Assuming

import java.util.UUID;

public interface CartService {

    /**
     * Retrieves or creates a cart for a given user.
     *
     * @param userId The Keycloak User ID of the cart owner.
     * @return The CartResponse DTO for the user's cart.
     */
    CartResponse getOrCreateCartByUserId(String userId);

    /**
     * Adds an item (application) to the specified user's cart or updates its quantity if it already exists.
     *
     * @param userId The Keycloak User ID of the cart owner.
     * @param addItemRequest DTO containing applicationId and quantity.
     * @return The updated CartResponse DTO.
     * @throws com.appverse.cart_service.exception.ResourceNotFoundException if the application to add is not found.
     * @throws com.appverse.cart_service.exception.ProductUnavailableException if the product cannot be added (e.g. out of stock, not for sale)
     */
    CartResponse addItemToCart(String userId, AddItemToCartRequest addItemRequest);

    /**
     * Updates the quantity of an existing item in the user's cart.
     * If quantity is 0, the item is removed.
     *
     * @param userId The Keycloak User ID of the cart owner.
     * @param applicationId The ID of the application (cart item) to update.
     * @param updateQuantityRequest DTO containing the new quantity.
     * @return The updated CartResponse DTO.
     * @throws com.appverse.cart_service.exception.ResourceNotFoundException if the cart or item is not found.
     */
    CartResponse updateCartItemQuantity(String userId, String applicationId, UpdateCartItemQuantityRequest updateQuantityRequest);

    /**
     * Removes an item completely from the user's cart.
     *
     * @param userId The Keycloak User ID of the cart owner.
     * @param applicationId The ID of the application (cart item) to remove.
     * @return The updated CartResponse DTO.
     * @throws com.appverse.cart_service.exception.ResourceNotFoundException if the cart or item is not found.
     */
    CartResponse removeItemFromCart(String userId, String applicationId);

    /**
     * Clears all items from the specified user's cart.
     *
     * @param userId The Keycloak User ID of the cart owner.
     * @return The (now empty) CartResponse DTO.
     * @throws com.appverse.cart_service.exception.ResourceNotFoundException if the cart is not found.
     */
    CartResponse clearCart(String userId);

    /**
     * Merges a guest cart (identified by a guest cart ID) into a logged-in user's cart.
     * The guest cart is typically deleted after a successful merge.
     * (This is a more advanced feature if you support guest checkouts/carts)
     *
     * @param guestCartId The ID of the guest cart.
     * @param userId The Keycloak User ID of the logged-in user.
     * @return The merged and updated CartResponse DTO for the user.
     */
    // CartResponse mergeGuestCart(UUID guestCartId, String userId); // Example for guest cart merging
}