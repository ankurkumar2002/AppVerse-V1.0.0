package com.appverse.cart_service.controller;

import com.appverse.cart_service.dto.AddItemToCartRequest;
import com.appverse.cart_service.dto.CartResponse;
import com.appverse.cart_service.dto.UpdateCartItemQuantityRequest;
import com.appverse.cart_service.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

// Assuming CartService is in com.appverse.cart_service.service

@RestController
@RequestMapping("/api/v1/carts") // Base path for cart operations
@RequiredArgsConstructor
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;

    /**
     * Retrieves the cart for the currently authenticated user.
     * If a cart doesn't exist for the user, one will be created.
     */
    @GetMapping("/mine")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> getMyCart(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject(); // 'sub' claim typically holds the Keycloak User ID
        log.info("Request to get or create cart for user ID: {}", userId);
        CartResponse cartResponse = cartService.getOrCreateCartByUserId(userId);
        return ResponseEntity.ok(cartResponse);
    }

    /**
     * Adds an item to the currently authenticated user's cart.
     */
    @PostMapping("/mine/items")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> addItemToMyCart(
            @Valid @RequestBody AddItemToCartRequest addItemRequest,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Request to add item (Application ID: {}, Quantity: {}) to cart for user ID: {}",
                addItemRequest.applicationId(), addItemRequest.quantity(), userId);
        CartResponse updatedCart = cartService.addItemToCart(userId, addItemRequest);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Updates the quantity of a specific item in the currently authenticated user's cart.
     * If newQuantity is 0, the item is removed.
     */
    @PutMapping("/mine/items/{applicationId}")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> updateMyCartItemQuantity(
            @PathVariable String applicationId,
            @Valid @RequestBody UpdateCartItemQuantityRequest updateQuantityRequest,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Request to update quantity of item (Application ID: {}) to {} for user ID: {}",
                applicationId, updateQuantityRequest.newQuantity(), userId);
        CartResponse updatedCart = cartService.updateCartItemQuantity(userId, applicationId, updateQuantityRequest);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Removes a specific item from the currently authenticated user's cart.
     */
    @DeleteMapping("/mine/items/{applicationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> removeItemFromMyCart(
            @PathVariable String applicationId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Request to remove item (Application ID: {}) from cart for user ID: {}", applicationId, userId);
        CartResponse updatedCart = cartService.removeItemFromCart(userId, applicationId);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Clears all items from the currently authenticated user's cart.
     */
    @DeleteMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> clearMyCart(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Request to clear cart for user ID: {}", userId);
        CartResponse clearedCart = cartService.clearCart(userId);
        return ResponseEntity.ok(clearedCart);
    }

    // --- Admin Endpoints (Example - secure these appropriately) ---
    // These would typically require an ADMIN role.

    /**
     * Admin endpoint to get any user's cart by their user ID.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_read:carts:all')") // Example admin/system scope
    public ResponseEntity<CartResponse> getCartByUserIdForAdmin(@PathVariable String userId) {
        log.info("Admin request to get cart for user ID: {}", userId);
        CartResponse cartResponse = cartService.getOrCreateCartByUserId(userId); // or a method that doesn't auto-create
        return ResponseEntity.ok(cartResponse);
    }
}