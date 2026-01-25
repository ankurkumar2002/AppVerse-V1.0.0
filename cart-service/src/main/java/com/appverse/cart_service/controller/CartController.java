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


@RestController
@RequestMapping("/api/v1/carts") 
@RequiredArgsConstructor
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;

    
    @GetMapping("/mine")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> getMyCart(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Request to get or create cart for user ID: {}", userId);
        CartResponse cartResponse = cartService.getOrCreateCartByUserId(userId);
        return ResponseEntity.ok(cartResponse);
    }

 
    @PostMapping("/mine/items")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> addItemToMyCart(
            @Valid @RequestBody AddItemToCartRequest addItemRequest,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Request to add item (Application ID: {}, Quantity: {}) to cart for user ID: {}",
                addItemRequest.applicationId(), addItemRequest.quantity(), userId);
        CartResponse updatedCart = cartService.addItemTocart(userId, addItemRequest);
        return ResponseEntity.ok(updatedCart);
    }


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


    @DeleteMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> clearMyCart(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Request to clear cart for user ID: {}", userId);
        CartResponse clearedCart = cartService.clearCart(userId);
        return ResponseEntity.ok(clearedCart);
    }


    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_read:carts:all')")
    public ResponseEntity<CartResponse> getCartByUserIdForAdmin(@PathVariable String userId) {
        log.info("Admin request to get cart for user ID: {}", userId);
        CartResponse cartResponse = cartService.getOrCreateCartByUserId(userId);
        return ResponseEntity.ok(cartResponse);
    }
}