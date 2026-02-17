package com.appverse.api_gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.appverse.api_gateway.controller.FallBackController;

public class FallBackControllerTest {
    private final FallBackController controller = new FallBackController();

    @Test
    void shouldReturnServiceUnavailable() {
        ResponseEntity<String> response = controller.fallback();

        assertEquals(503, response.getStatusCode().value());
        assertEquals("Service temporarily unavailable. Try again later.", response.getBody());
    }
}
