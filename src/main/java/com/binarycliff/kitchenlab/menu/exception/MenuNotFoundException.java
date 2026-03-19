package com.binarycliff.kitchenlab.menu.exception;

/**
 * Exception thrown when a requested menu entity (category or item) is not found.
 * Typically results in HTTP 404 Not Found response.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
public class MenuNotFoundException extends MenuException {

    private final String entityType;
    private final Object entityId;

    public MenuNotFoundException(String entityType, Object entityId) {
        super("MENU_NOT_FOUND", String.format("%s with ID '%s' not found", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public MenuNotFoundException(String entityType, String fieldName, Object fieldValue) {
        super("MENU_NOT_FOUND", String.format("%s with %s '%s' not found", entityType, fieldName, fieldValue));
        this.entityType = entityType;
        this.entityId = fieldValue;
    }

    public String getEntityType() {
        return entityType;
    }

    public Object getEntityId() {
        return entityId;
    }
}
