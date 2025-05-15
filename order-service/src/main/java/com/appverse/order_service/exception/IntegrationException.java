package com.appverse.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for issues encountered during integration with external services,
 * such as Keycloak.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Or HttpStatus.BAD_GATEWAY (502) might be more fitting
public class IntegrationException extends RuntimeException {

    public IntegrationException(String message) {
        super(message);
    }

    public IntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}