package com.binarycliff.kitchenlab.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Standard error response wrapper for all API endpoints.
 * Follows consistent error response format standards.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success = false;
    private String error;
    private String message;
    private List<String> details;
    private Map<String, Object> metadata;
    private Instant timestamp = Instant.now();
    private String path;
    private int status;

    /**
     * Create validation error response.
     * 
     * @param message error message
     * @param details validation error details
     * @return error response
     */
    public static ErrorResponse validationError(String message, List<String> details) {
        return ErrorResponse.builder()
                .error("VALIDATION_ERROR")
                .message(message)
                .details(details)
                .build();
    }

    /**
     * Create authentication error response.
     * 
     * @param message error message
     * @return error response
     */
    public static ErrorResponse authenticationError(String message) {
        return ErrorResponse.builder()
                .error("AUTHENTICATION_ERROR")
                .message(message)
                .build();
    }

    /**
     * Create authorization error response.
     * 
     * @param message error message
     * @return error response
     */
    public static ErrorResponse authorizationError(String message) {
        return ErrorResponse.builder()
                .error("AUTHORIZATION_ERROR")
                .message(message)
                .build();
    }

    /**
     * Create resource not found error response.
     * 
     * @param message error message
     * @return error response
     */
    public static ErrorResponse notFoundError(String message) {
        return ErrorResponse.builder()
                .error("NOT_FOUND_ERROR")
                .message(message)
                .build();
    }

    /**
     * Create business logic error response.
     * 
     * @param message error message
     * @return error response
     */
    public static ErrorResponse businessError(String message) {
        return ErrorResponse.builder()
                .error("BUSINESS_ERROR")
                .message(message)
                .build();
    }

    /**
     * Create internal server error response.
     * 
     * @param message error message
     * @return error response
     */
    public static ErrorResponse internalError(String message) {
        return ErrorResponse.builder()
                .error("INTERNAL_ERROR")
                .message(message)
                .build();
    }
}
