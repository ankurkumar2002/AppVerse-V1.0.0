// === In Payment Service Project ===
package com.appverse.subscription_service.exception; // Or your preferred exceptions package

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate an error during payment processing.
 * This can be used for issues like gateway communication errors,
 * unexpected states, or business rule violations specific to payments.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Default to 500, can be overridden by GlobalExceptionHandler
public class PaymentProcessingException extends RuntimeException {

    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}