
package com.appverse.cart_service.service.serviceImpl;

import com.appverse.cart_service.client.ApplicationServiceClient;
import com.appverse.cart_service.dto.AddItemToCartRequest;
import com.appverse.cart_service.dto.ApplicationDetails;
import com.appverse.cart_service.dto.CartResponse;
import com.appverse.cart_service.dto.UpdateCartItemQuantityRequest;
import com.appverse.cart_service.event.payload.*;
import com.appverse.cart_service.exception.DatabaseOperationException;
import com.appverse.cart_service.exception.ProductUnavailableException;
import com.appverse.cart_service.exception.ResourceNotFoundException;
import com.appverse.cart_service.mapper.CartMapper;
import com.appverse.cart_service.model.Cart;
import com.appverse.cart_service.model.CartItem;
import com.appverse.cart_service.publisher.KafkaEventPublisher;
import com.appverse.cart_service.repository.CartRepository;
import com.appverse.cart_service.service.CartService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartServiceImpl implements CartService {

        private final CartRepository cartRepository;
        private final ApplicationServiceClient applicationServiceClient;
        private final CartMapper cartMapper;
        private final KafkaEventPublisher kafkaEventPublisher;

        private static final String CART_EVENTS_TOPIC = "cart-events";

        @Override
        @Cacheable(value = "cartByUser", key = "#userId")
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
                        return savedCart;
                } catch (DataAccessException e) {
                        log.error("Database error creating new cart for user {}: {}", userId, e.getMessage(), e);
                        throw new DatabaseOperationException("Could not create cart for user. " + e.getMessage() + e);
                }
        }

        @Override
        @CacheEvict(value = "cartByUser", key = "#userId")
        @Transactional
        public CartResponse addItemTocart(String userId, AddItemToCartRequest addItemToRequest) {
                log.info("User {} attempting to add item (AppID: {}, Qty: {}) to cart.",
                                userId, addItemToRequest.applicationId(), addItemToRequest.quantity());

                ApplicationDetails appDetails = fetchApplicationDetails(
                                addItemToRequest.applicationId());

                return addItemToCartInternal(userId, addItemToRequest, appDetails);
        }

        @Transactional
        protected CartResponse addItemToCartInternal(
                        String userId,
                        AddItemToCartRequest addItemToCartRequest,
                        ApplicationDetails appDetails) {
                Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> createNewCart(userId));

                Optional<CartItem> existingItemOpt = cart.getItems().stream()
                                .filter(item -> item.getApplicationId().equals(addItemToCartRequest.applicationId()))
                                .findFirst();

                CartItem itemForEvent;
                int quantityAdded = addItemToCartRequest.quantity();

                if (existingItemOpt.isPresent()) {
                        CartItem existingItem = existingItemOpt.get();
                        existingItem.setQuantity(existingItem.getQuantity() + quantityAdded);
                        existingItem.setAddedAt(Instant.now());
                        itemForEvent = existingItem;
                } else {
                        CartItem newItem = CartItem.builder()
                                        .applicationId(appDetails.id())
                                        .applicationName(appDetails.name())
                                        .quantity(quantityAdded)
                                        .unitPrice(appDetails.price())
                                        .currency(appDetails.currency())
                                        .isFree(appDetails.isFree())
                                        .thumbnailUrl(appDetails.thumbnailUrl())
                                        .addedAt(Instant.now())
                                        .build();
                        cart.addItem(newItem);
                        itemForEvent = newItem;
                }

                Cart savedCart = cartRepository.save(cart);

                CartItem savedItem = savedCart.getItems().stream()
                                .filter(i -> i.getApplicationId().equals(itemForEvent.getApplicationId()))
                                .findFirst()
                                .orElse(itemForEvent);

                publishAfterCommit(() -> {
                        CartItemAddedPayload payload = new CartItemAddedPayload(
                                        savedCart.getId().toString(),
                                        userId,
                                        savedItem.getId().toString(),
                                        savedItem.getApplicationId(),
                                        savedItem.getApplicationName(),
                                        quantityAdded,
                                        savedItem.getQuantity(),
                                        savedItem.getUnitPrice(),
                                        savedItem.getCurrency(),
                                        Instant.now());
                        kafkaEventPublisher.publishAfterCommit(CART_EVENTS_TOPIC, savedCart.getId().toString(),
                                        payload);
                        log.info("Published CartItemAddedEvent for Cart {}", savedCart.getId());
                });

                return cartMapper.toCartResponse(savedCart);
        }

        private ApplicationDetails fetchApplicationDetails(String applicationId) {
                try {
                        log.debug("Fetching application details for ID: {}", applicationId);
                        ApplicationDetails details = applicationServiceClient
                                        .getApplicationDetails(applicationId);

                        if (details == null) {
                                throw new ResourceNotFoundException(
                                                "Application with ID " + applicationId + " now found.");
                        }
                        return details;
                } catch (FeignException.NotFound e) {
                        throw new ResourceNotFoundException(
                                        "Application with ID " + applicationId + " not found. " + e);
                } catch (FeignException e) {
                        throw new ProductUnavailableException("Application service unavailable.", e);
                }
        }

        @Override
        @CacheEvict(value = "cartByUser", key = "#userId")
        @Transactional
        public CartResponse updateCartItemQuantity(
                        String userId,
                        String applicationId,
                        UpdateCartItemQuantityRequest updateRequest) {

                Cart cart = cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cart not found for user: " + userId));

                CartItem item = cart.getItems().stream()
                                .filter(i -> i.getApplicationId().equals(applicationId))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Item not found in cart"));

                int oldQuantity = item.getQuantity();
                boolean removed = false;

                if (updateRequest.newQuantity() <= 0) {
                        cart.removeItem(item);
                        removed = true;
                } else {
                        item.setQuantity(updateRequest.newQuantity());
                        item.setAddedAt(Instant.now());
                }

                Cart savedCart = cartRepository.save(cart);

                if (removed) {
                        CartItemRemovedPayload payload = new CartItemRemovedPayload(
                                        savedCart.getId().toString(),
                                        userId,
                                        item.getId().toString(),
                                        applicationId,
                                        item.getApplicationName(),
                                        oldQuantity,
                                        Instant.now());

                        kafkaEventPublisher.publishAfterCommit(
                                        CART_EVENTS_TOPIC,
                                        savedCart.getId().toString(),
                                        payload);
                } else {
                        CartItemQuantityUpdatedPayload payload = new CartItemQuantityUpdatedPayload(
                                        savedCart.getId().toString(),
                                        userId,
                                        item.getId().toString(),
                                        applicationId,
                                        oldQuantity,
                                        item.getQuantity(),
                                        Instant.now());

                        kafkaEventPublisher.publishAfterCommit(
                                        CART_EVENTS_TOPIC,
                                        savedCart.getId().toString(),
                                        payload);
                }

                return cartMapper.toCartResponse(savedCart);
        }

        @Override
        @CacheEvict(value = "cartByUser", key = "#userId")
        @Transactional
        public CartResponse removeItemFromCart(String userId, String applicationId) {

                Cart cart = cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cart not found for user: " + userId));

                CartItem item = cart.getItems().stream()
                                .filter(i -> i.getApplicationId().equals(applicationId))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Item not found in cart"));

                int removedQuantity = item.getQuantity();

                cart.removeItem(item);

                Cart savedCart = cartRepository.save(cart);

                CartItemRemovedPayload payload = new CartItemRemovedPayload(
                                savedCart.getId().toString(),
                                userId,
                                item.getId().toString(),
                                applicationId,
                                item.getApplicationName(),
                                removedQuantity,
                                Instant.now());

                kafkaEventPublisher.publishAfterCommit(
                                CART_EVENTS_TOPIC,
                                savedCart.getId().toString(),
                                payload);

                return cartMapper.toCartResponse(savedCart);
        }

        @Override
        @CacheEvict(value = "cartByUser", key = "#userId")
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
                        return cartMapper.toCartResponse(cart);
                }

                cart.getItems().clear();

                try {
                        Cart updatedCart = cartRepository.save(cart);
                        log.info("Cart {} for user {} cleared. {} items removed.", updatedCart.getId(), userId,
                                        numberOfItemsCleared);

                        CartClearedPayload payload = new CartClearedPayload(
                                        updatedCart.getId().toString(),
                                        userId,
                                        numberOfItemsCleared,
                                        clearedApplicationIds,
                                        Instant.now());
                        kafkaEventPublisher.publishAfterCommit(CART_EVENTS_TOPIC, updatedCart.getId().toString(),
                                        payload);
                        log.info("Published CartClearedEvent for Cart ID: {}", updatedCart.getId());

                        return cartMapper.toCartResponse(updatedCart);
                } catch (DataAccessException e) {
                        log.error("Database error clearing cart for user {}: {}", userId, e.getMessage(), e);
                        throw new DatabaseOperationException("Could not clear cart. " + e.getMessage() + e);
                }
        }

        public void publishAfterCommit(Runnable action) {
                TransactionSynchronizationManager.registerSynchronization(
                                new TransactionSynchronization() {
                                        @Override
                                        public void afterCommit() {
                                                action.run();
                                        }
                                });
        }
}