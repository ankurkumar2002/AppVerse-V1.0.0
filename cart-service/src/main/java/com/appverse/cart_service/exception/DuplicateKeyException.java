package com.appverse.cart_service.exception;

public class DuplicateKeyException extends RuntimeException{
    public DuplicateKeyException(String message){
        super(message);
    }
}
