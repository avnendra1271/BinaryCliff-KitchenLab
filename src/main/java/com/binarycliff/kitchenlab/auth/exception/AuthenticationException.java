package com.binarycliff.kitchenlab.auth.exception;

import lombok.Getter;

/**
 * Base exception for authentication operations.
 */
@Getter
public class AuthenticationException extends RuntimeException {
    
    private final String errorCode;
    
    public AuthenticationException(String message) {
        super(message);
        this.errorCode = "AUTH_ERROR";
    }
    
    public AuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTH_ERROR";
    }
    
    public AuthenticationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
