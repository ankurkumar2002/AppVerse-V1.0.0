package com.appverse.order_service.exception;

public class UpdateOperationException extends RuntimeException{
    public UpdateOperationException(String message){
        super(message);
    }
}
