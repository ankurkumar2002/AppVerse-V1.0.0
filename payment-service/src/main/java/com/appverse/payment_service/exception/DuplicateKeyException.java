package com.appverse.payment_service.exception;


public class DuplicateKeyException extends RuntimeException{
    public DuplicateKeyException(String message){
        super(message);
    }
}
