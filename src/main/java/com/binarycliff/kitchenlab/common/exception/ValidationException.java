package com.binarycliff.kitchenlab.common.exception;

/**
 * Exception thrown for validation errors.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
