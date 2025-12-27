package com.appverse.developer_service.model;

import java.time.Instant;

public record Error_Response( Instant timestamp,
        int status,
        String error,
        String message) {
    
}
