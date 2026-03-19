package com.binarycliff.kitchenlab.menu.exception;

/**
 * Exception thrown when menu data validation fails.
 * Typically results in HTTP 400 Bad Request response.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
public class InvalidMenuDataException extends MenuException {

    private final String fieldName;
    private final Object fieldValue;

    public InvalidMenuDataException(String message) {
        super("INVALID_MENU_DATA", message);
        this.fieldName = null;
        this.fieldValue = null;
    }

    public InvalidMenuDataException(String fieldName, Object fieldValue, String message) {
        super("INVALID_MENU_DATA", message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
