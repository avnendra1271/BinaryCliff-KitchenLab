package com.binarycliff.kitchenlab.auth.exception;

import lombok.Getter;

/**
 * Exception thrown when validation fails.
 */
@Getter
public class ValidationException extends AuthenticationException {
    
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, "VALIDATION_ERROR", cause);
    }
}
