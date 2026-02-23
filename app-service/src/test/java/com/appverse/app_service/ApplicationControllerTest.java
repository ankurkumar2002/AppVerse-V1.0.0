package com.appverse.app_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.appverse.app_service.services.ApplicationService;
import com.appverse.app_service.controller.ApplicationController;
import com.appverse.app_service.dto.ApplicationResponse;
import com.appverse.app_service.repository.ApplicationRepository;

@WebMvcTest(ApplicationController.class)
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService service;

    @MockBean
    private ApplicationRepository repository;

    @Test
    void getById_shouldReturn200() throws Exception {

        ApplicationResponse response = new ApplicationResponse(
                "app1",
                "Test App",
                "desc",
                "1.0",
                null,
                null,
                null,
                null,
                false, null,
                null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(service.getApplicationById("app1"))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/apps/app1")
                        .with(jwt()))
                .andExpect(status().isOk());
    }

}
