// === In cart-service Project ===
package com.appverse.cart_service.service.serviceImpl;

import com.appverse.cart_service.client.ApplicationServiceClient;
import com.appverse.cart_service.dto.AddItemToCartRequest;
import com.appverse.cart_service.dto.CartResponse;
import com.appverse.cart_service.dto.UpdateCartItemQuantityRequest;
import com.appverse.cart_service.event.payload.*; // <<< IMPORT YOUR EVENT PAYLOADS
import com.appverse.cart_service.exception.DatabaseOperationException;
import com.appverse.cart_service.exception.ProductUnavailableException;
import com.appverse.cart_service.exception.ResourceNotFoundException;
import com.appverse.cart_service.mapper.CartMapper;
import com.appverse.cart_service.model.Cart;
import com.appverse.cart_service.model.CartItem;
import com.appverse.cart_service.repository.CartRepository;
import com.appverse.cart_service.service.CartService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <<< IMPORT FOR LOGGING
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List; // For CartClearedPayload
import java.util.Optional;
import java.util.stream.Collectors; // For CartClearedPayload

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j // <<< ADD ANNOTATION FOR LOGGING
public class CartServiceImpl implements CartService {

    // private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class); // Replaced by @Slf4j

    private final CartRepository cartRepository;
    private final ApplicationServiceClient applicationServiceClient;
    private final CartMapper cartMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate; // <<< INJECT KAFKA TEMPLATE

    private static final String CART_EVENTS_TOPIC = "cart-events"; // Define Kafka topic for cart events

    @Override
    @Transactional(readOnly = true)
    public CartResponse getOrCreateCartByUserId(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        return cartMapper.toCartResponse(cart);
    }

    private Cart createNewCart(String userId) {
        Cart newCart = Cart.builder().userId(userId).build();
        try {
            Cart savedCart = cartRepository.save(newCart);
            log.info("New cart {} created for user {}", savedCart.getId(), userId);
            // Optionally publish a CartCreatedEvent if other services need to know about new carts
            // For now, focusing on item-level events as requested.
            return savedCart;
        } catch (DataAccessException e) {
            log.error("Database error creating new cart for user {}: {}", userId, e.getMessage(), e);
            throw new DatabaseOperationException("Could not create cart for user. " + e.getMessage()+ e);
        }
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(String userId, AddItemToCartRequest addItemRequest) {
        log.info("User {} attempting to add item (AppID: {}, Qty: {}) to cart.",
                 userId, addItemRequest.applicationId(), addItemRequest.quantity());
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        ApplicationServiceClient.ApplicationDetails appDetails;
        try {
            log.debug("Fetching application details for ID: {}", addItemRequest.applicationId());
            appDetails = applicationServiceClient.getApplicationDetails(addItemRequest.applicationId());
            if (appDetails == null) {
                throw new ResourceNotFoundException("Application with ID " + addItemRequest.applicationId() + " not found.");
            }
            // You might add a check here: if (appDetails.isFree()) { don't add to cart / or handle differently }
            // Or if app is not purchasable/available.
        } catch (FeignException.NotFound e) {
            log.warn("Application not found via Feign client: AppID {}", addItemRequest.applicationId(), e);
            throw new ResourceNotFoundException("Application with ID " + addItemRequest.applicationId() + " not found."+ e);
        } catch (FeignException e) {
            log.error("Error fetching application details for AppID {}: Status {}, Message: {}",
                      addItemRequest.applicationId(), e.status(), e.getMessage(), e);
            throw new ProductUnavailableException("Could not retrieve application details. Service may be temporarily unavailable.", e);
        }

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getApplicationId().equals(addItemRequest.applicationId()))
                .findFirst();

        CartItem itemForEvent;
        int quantityAdded = addItemRequest.quantity();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            log.debug("Item AppID {} already in cart. Updating quantity.", addItemRequest.applicationId());
            existingItem.setQuantity(existingItem.getQuantity() + addItemRequest.quantity());
            existingItem.setAddedAt(Instant.now());
            itemForEvent = existingItem;
        } else {
            log.debug("Item AppID {} not in cart. Adding new item.", addItemRequest.applicationId());
            CartItem newItem = CartItem.builder()
                    .applicationId(appDetails.id())
                    .applicationName(appDetails.name()) // Denormalized
                    .quantity(addItemRequest.quantity())
                    .unitPrice(appDetails.price())     // Denormalized price at time of adding
                    .currency(appDetails.currency())   // Denormalized
                    .isFree(appDetails.isFree())       // Denormalized
                    .thumbnailUrl(appDetails.thumbnailUrl()) // Denormalized
                    .addedAt(Instant.now())
                    .build();
            cart.addItem(newItem); // Sets bidirectional link and adds to cart's item list
            itemForEvent = newItem; // The CartItem entity might not have its ID until after save if ID is generated
        }

        try {
            Cart updatedCart = cartRepository.save(cart);
            log.info("Item AppID {} added/updated in cart {} for user {}. New total quantity for item: {}",
                     addItemRequest.applicationId(), updatedCart.getId(), userId, itemForEvent.getQuantity());

            // Find the saved item to get its generated ID for the event
            CartItem savedItemForEvent = updatedCart.getItems().stream()
                .filter(i -> i.getApplicationId().equals(itemForEvent.getApplicationId()))
                .findFirst()
                .orElse(itemForEvent); // Fallback, though it should be found

            // --- Publish CartItemAddedEvent ---
            CartItemAddedPayload payload = new CartItemAddedPayload(
                    updatedCart.getId().toString(), // Assuming Cart ID is UUID
                    userId,
                    savedItemForEvent.getId().toString(), // Assuming CartItem ID is UUID
                    savedItemForEvent.getApplicationId(),
                    savedItemForEvent.getApplicationName(),
                    quantityAdded, // The quantity from the request for this operation
                    savedItemForEvent.getQuantity(), // The new total quantity of this item
                    savedItemForEvent.getUnitPrice(),
                    savedItemForEvent.getCurrency(),
                    Instant.now()
            );
            kafkaTemplate.send(CART_EVENTS_TOPIC, updatedCart.getId().toString(), payload); // Key by cart ID
            log.info("Published CartItemAddedEvent for Cart ID: {}, Item AppID: {}", updatedCart.getId(), savedItemForEvent.getApplicationId());

            return cartMapper.toCartResponse(updatedCart);
        } catch (DataAccessException e) {
            log.error("Database error adding/updating item in cart for user {}: {}", userId, e.getMessage(), e);
            throw new DatabaseOperationException("Could not update cart. " + e.getMessage()+ e);
        }
    }

    @Override
    @Transactional
    public CartResponse updateCartItemQuantity(String userId, String applicationId, UpdateCartItemQuantityRequest updateRequest) {
        log.info("User {} updating quantity for AppID {} in cart to {}.", userId, applicationId, updateRequest.newQuantity());
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getApplicationId().equals(applicationId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item with application ID " + applicationId + " not found in cart."));

        int oldQuantity = itemToUpdate.getQuantity();
        boolean itemRemoved = false;

        if (updateRequest.newQuantity() <= 0) {
            log.debug("New quantity for AppID {} is <= 0. Removing item from cart.", applicationId);
            cart.removeItem(itemToUpdate); // Manages bidirectional link and orphanRemoval
            itemRemoved = true;
        } else {
            log.debug("Updating quantity for AppID {} from {} to {}.", applicationId, oldQuantity, updateRequest.newQuantity());
            itemToUpdate.setQuantity(updateRequest.newQuantity());
            itemToUpdate.setAddedAt(Instant.now()); // Update timestamp of last interaction
        }

        try {
            Cart updatedCart = cartRepository.save(cart);
            log.info("Cart {} for user {} updated. Item AppID {} quantity changed or item removed.",
                     updatedCart.getId(), userId, applicationId);

            if (itemRemoved) {
                // --- Publish CartItemRemovedEvent ---
                CartItemRemovedPayload payload = new CartItemRemovedPayload(
                        updatedCart.getId().toString(),
                        userId,
                        itemToUpdate.getId().toString(), // ID of the removed item
                        applicationId,
                        itemToUpdate.getApplicationName(), // Name before removal
                        oldQuantity, // Quantity it had before removal
                        Instant.now()
                );
                kafkaTemplate.send(CART_EVENTS_TOPIC, updatedCart.getId().toString(), payload);
                log.info("Published CartItemRemovedEvent for Cart ID: {}, Item AppID: {}", updatedCart.getId(), applicationId);
            } else {
                // --- Publish CartItemQuantityUpdatedEvent ---
                CartItemQuantityUpdatedPayload payload = new CartItemQuantityUpdatedPayload(
                        updatedCart.getId().toString(),
                        userId,
                        itemToUpdate.getId().toString(),
                        applicationId,
                        oldQuantity,
                        itemToUpdate.getQuantity(), // New quantity
                        Instant.now()
                );
                kafkaTemplate.send(CART_EVENTS_TOPIC, updatedCart.getId().toString(), payload);
                log.info("Published CartItemQuantityUpdatedEvent for Cart ID: {}, Item AppID: {}", updatedCart.getId(), applicationId);
            }
            return cartMapper.toCartResponse(updatedCart);
        } catch (DataAccessException e) {
            log.error("Database error updating item quantity in cart for user {}: {}", userId, e.getMessage(), e);
            throw new DatabaseOperationException("Could not update item quantity. " + e.getMessage()+ e);
        }
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(String userId, String applicationId) {
        log.info("User {} removing item AppID {} from cart.", userId, applicationId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getApplicationId().equals(applicationId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item with application ID " + applicationId + " not found in cart."));

        String removedItemId = itemToRemove.getId().toString(); // Get ID before removal
        String removedItemName = itemToRemove.getApplicationName();
        int removedQuantity = itemToRemove.getQuantity();

        cart.removeItem(itemToRemove);

        try {
            Cart updatedCart = cartRepository.save(cart);
            log.info("Item AppID {} removed from cart {} for user {}.", applicationId, updatedCart.getId(), userId);

            // --- Publish CartItemRemovedEvent ---
            CartItemRemovedPayload payload = new CartItemRemovedPayload(
                    updatedCart.getId().toString(),
                    userId,
                    removedItemId, // ID of the removed item
                    applicationId,
                    removedItemName,
                    removedQuantity,
                    Instant.now()
            );
            kafkaTemplate.send(CART_EVENTS_TOPIC, updatedCart.getId().toString(), payload);
            log.info("Published CartItemRemovedEvent for Cart ID: {}, Item AppID: {}", updatedCart.getId(), applicationId);

            return cartMapper.toCartResponse(updatedCart);
        } catch (DataAccessException e) {
            log.error("Database error removing item from cart for user {}: {}", userId, e.getMessage(), e);
            throw new DatabaseOperationException("Could not remove item from cart. " + e.getMessage()+ e);
        }
    }

    @Override
    @Transactional
    public CartResponse clearCart(String userId) {
        log.info("User {} clearing their cart.", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        List<String> clearedApplicationIds = cart.getItems().stream()
                                                .map(CartItem::getApplicationId)
                                                .collect(Collectors.toList());
        int numberOfItemsCleared = cart.getItems().size();

        if (numberOfItemsCleared == 0) {
            log.info("Cart for user {} was already empty. No action taken.", userId);
            return cartMapper.toCartResponse(cart); // Return current empty cart
        }

        cart.getItems().clear(); // Relies on orphanRemoval=true

        try {
            Cart updatedCart = cartRepository.save(cart); // Persist the changes (empty item list)
            log.info("Cart {} for user {} cleared. {} items removed.", updatedCart.getId(), userId, numberOfItemsCleared);

            // --- Publish CartClearedEvent ---
            CartClearedPayload payload = new CartClearedPayload(
                    updatedCart.getId().toString(),
                    userId,
                    numberOfItemsCleared,
                    clearedApplicationIds,
                    Instant.now()
            );
            kafkaTemplate.send(CART_EVENTS_TOPIC, updatedCart.getId().toString(), payload);
            log.info("Published CartClearedEvent for Cart ID: {}", updatedCart.getId());

            return cartMapper.toCartResponse(updatedCart);
        } catch (DataAccessException e) {
            log.error("Database error clearing cart for user {}: {}", userId, e.getMessage(), e);
            throw new DatabaseOperationException("Could not clear cart. " + e.getMessage()+ e);
        }
    }
}