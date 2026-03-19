package com.binarycliff.kitchenlab.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard success response wrapper for all API endpoints.
 * Follows consistent response format standards.
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
public class SuccessResponse<T> {

    private boolean success = true;
    private String message;
    private T data;
    private Instant timestamp = Instant.now();
    
    /**
     * Create success response with data.
     * 
     * @param message success message
     * @param data response data
     * @return success response
     */
    public static <T> SuccessResponse<T> of(String message, T data) {
        return SuccessResponse.<T>builder()
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * Create success response without data.
     * 
     * @param message success message
     * @return success response
     */
    public static <T> SuccessResponse<T> of(String message) {
        return SuccessResponse.<T>builder()
                .message(message)
                .build();
    }
}
