package com.appverse.cart_service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.appverse.cart_service.config.SecurityConfig;
import com.appverse.cart_service.controller.CartController;
import com.appverse.cart_service.dto.CartResponse;
import com.appverse.cart_service.service.CartService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CartControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Test
    void shouldReturnCartForUser() throws Exception{
        when(cartService.getOrCreateCartByUserId("user123")).thenReturn(mock(CartResponse.class));

        mockMvc.perform(get("/api/v1/carts/mine")
                        .with(jwt().jwt(jwt -> jwt.subject("user123"))
                            .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk());
    }
}
