package com.binarycliff.kitchenlab.auth.exception;

/**
 * Exception thrown when user authentication fails.
 */
public class UserNotFoundException extends AuthenticationException {
    
    public UserNotFoundException(String username) {
        super("User not found: " + username, "USER_NOT_FOUND");
    }
    
    public UserNotFoundException(String message, String identifier) {
        super(message, "USER_NOT_FOUND");
    }
}
