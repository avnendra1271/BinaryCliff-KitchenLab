package com.binarycliff.kitchenlab.auth.exception;

/**
 * Exception thrown when account is locked due to rate limiting.
 */
public class AccountLockedException extends AuthenticationException {
    
    public AccountLockedException(long remainingMinutes) {
        super("Account locked. Try again in " + remainingMinutes + " minutes.", "ACCOUNT_LOCKED");
    }
}
