package com.appverse.cart_service.exception;
public class DatabaseOperationException extends RuntimeException{
    public DatabaseOperationException(String message){
        super(message);
    }
}
