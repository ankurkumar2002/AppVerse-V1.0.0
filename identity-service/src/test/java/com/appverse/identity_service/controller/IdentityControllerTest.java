package com.appverse.identity_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.appverse.identity_service.dto.IdentityUserResponse;
import com.appverse.identity_service.service.IdentityService;

@WebMvcTest(
    controllers = IdentityController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
class IdentityControllerTest {

    @MockBean
    private IdentityService identityService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnCurrentUser() throws Exception {
        IdentityUserResponse response =
                new IdentityUserResponse(
                        "1",
                        "ankur",
                        "a@test.com",
                        true,
                        "Ankur",
                        "Singh"
                );

        when(identityService.getCurrentUser(any()))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/identity/users/me")
                    .with(jwt())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("ankur"));
    }
}