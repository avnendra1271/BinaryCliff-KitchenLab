package com.binarycliff.kitchenlab.menu.exception;

/**
 * Base exception class for menu-related operations.
 * Provides common functionality for all menu exceptions.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
public class MenuException extends RuntimeException {

    private final String errorCode;

    public MenuException(String message) {
        super(message);
        this.errorCode = "MENU_ERROR";
    }

    public MenuException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MENU_ERROR";
    }

    public MenuException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MenuException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
