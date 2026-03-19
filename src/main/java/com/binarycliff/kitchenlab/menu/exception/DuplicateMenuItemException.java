package com.binarycliff.kitchenlab.menu.exception;

/**
 * Exception thrown when attempting to create a duplicate menu entity.
 * Typically results in HTTP 409 Conflict response.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
public class DuplicateMenuItemException extends MenuException {

    private final String entityType;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateMenuItemException(String entityType, String fieldName, Object fieldValue) {
        super("DUPLICATE_MENU_ITEM", String.format("%s with %s '%s' already exists", entityType, fieldName, fieldValue));
        this.entityType = entityType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
