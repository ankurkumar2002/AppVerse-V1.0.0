package com.appverse.app_service.exception;

public class SomeErrorException extends RuntimeException {
    public SomeErrorException(String message){
        super(message);
    }
}
