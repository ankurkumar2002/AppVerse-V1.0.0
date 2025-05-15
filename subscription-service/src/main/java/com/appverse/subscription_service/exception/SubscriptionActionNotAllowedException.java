// === In Subscription Service Project ===
package com.appverse.subscription_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request or 409 Conflict might be suitable
public class SubscriptionActionNotAllowedException extends RuntimeException {
    public SubscriptionActionNotAllowedException(String message) {
        super(message);
    }

    public SubscriptionActionNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}