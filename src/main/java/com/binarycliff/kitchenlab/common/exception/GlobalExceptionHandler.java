package com.binarycliff.kitchenlab.common.exception;

import com.binarycliff.kitchenlab.auth.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST, 
                "Business Error", 
                ex.getMessage()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(com.binarycliff.kitchenlab.auth.exception.AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(com.binarycliff.kitchenlab.auth.exception.AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.UNAUTHORIZED, 
                ex.getErrorCode(), 
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND, 
                ex.getErrorCode(), 
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountLockedException(AccountLockedException ex) {
        log.error("Account locked: {}", ex.getMessage());
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS, 
                ex.getErrorCode(), 
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage());
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST, 
                ex.getErrorCode(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Bad credentials: {}", ex.getMessage());
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.UNAUTHORIZED, 
                "BAD_CREDENTIALS", 
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST, 
                "Validation Failed", 
                "Request validation failed"
        );
        errorResponse.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND, 
                "Resource Not Found", 
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Internal Server Error", 
                "An unexpected error occurred. Please try again later."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private Map<String, Object> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("message", message);
        return errorResponse;
    }
}
