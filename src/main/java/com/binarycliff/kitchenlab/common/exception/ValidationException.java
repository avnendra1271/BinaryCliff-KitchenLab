package com.binarycliff.kitchenlab.common.exception;

import lombok.Getter;

/**
 * Exception thrown for validation errors.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Getter
public class ValidationException extends RuntimeException {

    private final String errorCode;

    public ValidationException(String message) {
        super(message);
        this.errorCode = "VALIDATION_ERROR";
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "VALIDATION_ERROR";
    }

    public ValidationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ValidationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
