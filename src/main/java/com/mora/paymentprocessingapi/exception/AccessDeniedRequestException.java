package com.mora.paymentprocessingapi.exception;


public class AccessDeniedRequestException extends RuntimeException {

    public AccessDeniedRequestException(String message) {
        super(message);
    }
}
