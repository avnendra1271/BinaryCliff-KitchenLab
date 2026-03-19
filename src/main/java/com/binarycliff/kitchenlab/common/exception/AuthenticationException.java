package com.binarycliff.kitchenlab.common.exception;

/**
 * Exception thrown for authentication errors.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
