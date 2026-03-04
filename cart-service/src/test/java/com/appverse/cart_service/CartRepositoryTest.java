package com.appverse.cart_service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.appverse.cart_service.model.Cart;
import com.appverse.cart_service.repository.CartRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ImportAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class,
    org.springframework.cloud.openfeign.FeignAutoConfiguration.class
})
@ActiveProfiles("test")
public class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Test
    void shouldFindCartByUserId() {
        Cart cart = Cart.builder()
                .userId("user123")
                .build();

        cartRepository.save(cart);

        Optional<Cart> result = cartRepository.findByUserId("user123");

        assertTrue(result.isPresent());
    }
    
}
