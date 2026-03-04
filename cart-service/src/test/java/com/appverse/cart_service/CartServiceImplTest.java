package com.appverse.cart_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        doNothing().when(cartServiceImpl).publishAfterCommit(any());
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
    void shouldUpdateCartItemQuantity(){
        String userId = "user123";
        String appId = "app1";
        String cartId = "cart1";
        String cartItemId = "cartItemId1";
        UUID id = new UUID(2, 3);
        
        Cart cart = new Cart(id, userId, null,new ArrayList<>(),null, null);
        CartItem cartItem = new CartItem(id, cart, appId, "Test app", 1, new BigDecimal("500"), "INR", false, "http://google.com", Instant.now());

        
    }


}
