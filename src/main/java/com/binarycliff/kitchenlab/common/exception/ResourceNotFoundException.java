package com.binarycliff.kitchenlab.common.exception;

/**
 * Exception thrown when a resource is not found.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
