package com.binarycliff.kitchenlab.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response DTO.
 * Contains paginated data along with pagination metadata.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {

    private List<T> data;

    private int currentPage;

    private int totalPages;

    private long totalElements;

    private int pageSize;

    private boolean first;

    private boolean last;

    private boolean empty;
    private String message;

    /**
     * Create a paginated response from Spring Data Page.
     */
    public static <T> PaginatedResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return PaginatedResponse.<T>builder()
                .data(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
