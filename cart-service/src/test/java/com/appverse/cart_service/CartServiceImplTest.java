package com.appverse.cart_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.OptionalAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.appverse.cart_service.client.ApplicationServiceClient;
import com.appverse.cart_service.dto.AddItemToCartRequest;
import com.appverse.cart_service.dto.ApplicationDetails;
import com.appverse.cart_service.dto.CartResponse;
import com.appverse.cart_service.dto.UpdateCartItemQuantityRequest;
import com.appverse.cart_service.exception.ResourceNotFoundException;
import com.appverse.cart_service.mapper.CartMapper;
import com.appverse.cart_service.model.Cart;
import com.appverse.cart_service.publisher.KafkaEventPublisher;
import com.appverse.cart_service.repository.CartRepository;
import com.appverse.cart_service.service.serviceImpl.CartServiceImpl;
import com.appverse.cart_service.model.CartItem;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ApplicationServiceClient applicationServiceClient;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    @Spy
    @InjectMocks
    private CartServiceImpl cartServiceImpl;

    @BeforeEach
    void setup() {
        lenient().doNothing().when(cartServiceImpl).publishAfterCommit(any());
    }

    @Test
    void shouldAddNewItemToCart() {
        String userId = "user123";
        String appId = "app1";

        AddItemToCartRequest request = new AddItemToCartRequest(appId, 2);
        ApplicationDetails appDetails = new ApplicationDetails(
                appId,
                "Test App",
                null,
                null,
                null,
                null,
                BigDecimal.TEN,
                "USD",
                false,
                List.of(),
                null,
                null,
                null,
                "thumb.jpg",
                List.of(),
                null,
                null,
                null,
                List.of(),
                "ACTIVE",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                Double.valueOf(4.5),
                Integer.valueOf(10));

        Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        when(applicationServiceClient.getApplicationDetails(appId)).thenReturn(appDetails);

        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(cartMapper.toCartResponse(any())).thenReturn(mock(CartResponse.class));

        CartResponse response = cartServiceImpl.addItemTocart(userId, request);

        assertNotNull(response);
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());

        verify(cartRepository).save(cart);
    }

    @Test
    void shouldUpdateCartItemQuantity() {

        String userId = "user123";
        String appId = "app1";

        AddItemToCartRequest request = new AddItemToCartRequest(appId, 2);

        Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        CartItem cartItem = CartItem.builder()
                .id(UUID.randomUUID())
                .cart(cart)
                .applicationId(appId)
                .applicationName("Test App")
                .quantity(1)
                .unitPrice(new BigDecimal("500"))
                .currency("INR")
                .isFree(false)
                .thumbnailUrl("http://google.com")
                .addedAt(Instant.now())
                .build();

        cart.addItem(cartItem);

        ApplicationDetails appDetails = new ApplicationDetails(
                appId,
                "Test App",
                null,
                null,
                null,
                null,
                new BigDecimal("500"),
                "INR",
                false,
                List.of(),
                null,
                null,
                null,
                "thumb.jpg",
                List.of(),
                null,
                null,
                null,
                List.of(),
                "ACTIVE",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                4.5,
                10);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(applicationServiceClient.getApplicationDetails(appId)).thenReturn(appDetails);
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(cartMapper.toCartResponse(any())).thenReturn(mock(CartResponse.class));

        CartResponse response = cartServiceImpl.addItemTocart(userId, request);

        assertNotNull(response);
        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());

        verify(cartRepository).save(cart);

    }

    @Test
    void shouldRemmoveItemFromCart() {
        String userId = "user123";
        String appId = "app1";

        Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        CartItem cartItem = CartItem.builder()
                .id(UUID.randomUUID())
                .cart(cart)
                .applicationId(appId)
                .applicationName("Test App")
                .quantity(2)
                .unitPrice(new BigDecimal("500"))
                .currency("INR")
                .isFree(false)
                .thumbnailUrl("http://google.com")
                .addedAt(Instant.now())
                .build();

        cart.addItem(cartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(cartMapper.toCartResponse(any())).thenReturn(mock(CartResponse.class));

        CartResponse response = cartServiceImpl.removeItemFromCart(userId, appId);

        assertNotNull(response);
        assertEquals(0, cart.getItems().size());

        verify(cartRepository).save(cart);

    }

    @Test
    void shouldReturnExistingCart() {
        String userId = "user123";

        Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponse(cart)).thenReturn(mock(CartResponse.class));

        CartResponse response = cartServiceImpl.getOrCreateCartByUserId(userId);

        assertNotNull(response);
        verify(cartRepository).findByUserId(userId);
    }

    @Test
    void shouldCreateCartIfNotExists() {
        String userId = "user123";

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(cartMapper.toCartResponse(any())).thenReturn(mock(CartResponse.class));

        CartResponse response = cartServiceImpl.getOrCreateCartByUserId(userId);

        assertNotNull(response);
        verify(cartRepository).save(any());
    }

    @Test
    void shouldRemoveItemWhenQuantityZero() {
        String userId = "user123";
        String appId = "app1";

        Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .cart(cart)
                .applicationId(appId)
                .applicationName("Test App")
                .quantity(2)
                .unitPrice(BigDecimal.TEN)
                .currency("INR")
                .isFree(false)
                .addedAt(Instant.now())
                .build();

        cart.addItem(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenReturn(cart);
        when(cartMapper.toCartResponse(any())).thenReturn(mock(CartResponse.class));

        cartServiceImpl.updateCartItemQuantity(userId, appId, new UpdateCartItemQuantityRequest(0));

        assertEquals(0, cart.getItems().size());
    }

    @Test
    void shouldThrowWhenItemNotFound() {
        String userId = "user123";

        Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        assertThrows(ResourceNotFoundException.class,
                () -> cartServiceImpl.updateCartItemQuantity(
                        userId, "invalidApp", new UpdateCartItemQuantityRequest(2)));
    }

    @Test
    void shouldClearCart() {

        String userId = "user123";

        Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        cart.addItem(CartItem.builder()
                .id(UUID.randomUUID())
                .applicationId("app1")
                .applicationName("Test")
                .quantity(2)
                .unitPrice(BigDecimal.TEN)
                .currency("INR")
                .isFree(false)
                .addedAt(Instant.now())
                .build());

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenReturn(cart);
        when(cartMapper.toCartResponse(cart)).thenReturn(mock(CartResponse.class));

        CartResponse response = cartServiceImpl.clearCart(userId);

        assertNotNull(response);
        assertEquals(0, cart.getItems().size());
    }

    @Test
    void shouldHandleAlreadyEmptyCart() {
        String userId = "user123";

        Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponse(any())).thenReturn(mock(CartResponse.class));

        CartResponse cartResponse = cartServiceImpl.clearCart(userId);

        assertNotNull(cartResponse);
        assertEquals(0, cart.getItems().size());

    }

}
