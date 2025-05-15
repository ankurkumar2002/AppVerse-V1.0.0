package com.appverse.order_service.exception;

public class DatabaseOperationException extends RuntimeException{
    public DatabaseOperationException(String message){
        super(message);
    }
}
