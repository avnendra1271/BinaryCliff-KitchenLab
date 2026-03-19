package com.binarycliff.kitchenlab.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for menu statistics response.
 * Provides comprehensive analytics data for menu management.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuStatistics {

    private Long totalCategories;
    private Long activeCategories;
    private Long totalMenuItems;
    private Long activeMenuItems;
    private Long vegetarianItems;
    private Long popularItems;
    private Long itemsOnSale;
    private Double averagePrice;
    private Double averageCalories;
    private Long totalViews;
    private Long totalOrders;
    private UUID mostPopularCategoryId;
    private String mostPopularCategoryName;
    private UUID mostPopularItemId;
    private String mostPopularItemName;
}
