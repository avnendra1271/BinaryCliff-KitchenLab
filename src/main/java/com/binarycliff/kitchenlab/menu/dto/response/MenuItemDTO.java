package com.binarycliff.kitchenlab.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for menu item response.
 * Provides comprehensive menu item information for display.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {

    private UUID id;
    private String name;
    private String description;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String imageUrl;
    private Boolean isVegetarian;
    private Boolean isAvailable;
    private Boolean isPopular;
    private Boolean isSignature;
    private Integer displayOrder;
    private String allergens;
    private String ingredients;
    private String preparationTime;
    private Integer calories;
    private Integer spiceLevel;
    private Boolean customizable;
    private Long orderCount;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private Double discountPercentage;
    private String formattedPrice;
    private String formattedOriginalPrice;
    private String spiceLevelText;
    private Boolean hasDiscount;

    /**
     * Create a simple menu item DTO for list views.
     * 
     * @param id the menu item ID
     * @param name the item name
     * @param description the item description
     * @param categoryId the category ID
     * @param categoryName the category name
     * @param price the price
     * @param originalPrice the original price
     * @param imageUrl the image URL
     * @param isVegetarian vegetarian status
     * @param isAvailable availability status
     * @param isPopular popularity status
     * @param isSignature signature status
     * @param displayOrder the display order
     * @param spiceLevel the spice level
     * @return MenuItemDTO instance
     */
    public static MenuItemDTO createSimple(UUID id, String name, String description,
                                          UUID categoryId, String categoryName,
                                          BigDecimal price, BigDecimal originalPrice,
                                          String imageUrl, Boolean isVegetarian,
                                          Boolean isAvailable, Boolean isPopular,
                                          Boolean isSignature, Integer displayOrder,
                                          Integer spiceLevel) {
        MenuItemDTO dto = MenuItemDTO.builder()
                .id(id)
                .name(name)
                .description(description)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .price(price)
                .originalPrice(originalPrice)
                .imageUrl(imageUrl)
                .isVegetarian(isVegetarian)
                .isAvailable(isAvailable)
                .isPopular(isPopular)
                .isSignature(isSignature)
                .displayOrder(displayOrder)
                .spiceLevel(spiceLevel)
                .build();

        // Calculate computed fields
        dto.calculateComputedFields();
        return dto;
    }

    /**
     * Calculate computed fields from base data.
     */
    private void calculateComputedFields() {
        // Calculate discount percentage
        if (originalPrice != null && originalPrice.compareTo(price) > 0) {
            BigDecimal discount = originalPrice.subtract(price);
            discountPercentage = discount.divide(originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .doubleValue();
            hasDiscount = true;
        } else {
            discountPercentage = null;
            hasDiscount = false;
        }

        // Format prices
        formattedPrice = String.format("$%.2f", price);
        formattedOriginalPrice = originalPrice != null ? String.format("$%.2f", originalPrice) : null;

        // Get spice level text
        if (spiceLevel == null || spiceLevel < 0) {
            spiceLevelText = "Mild";
        } else {
            switch (spiceLevel) {
                case 0: case 1: spiceLevelText = "Mild"; break;
                case 2: spiceLevelText = "Medium"; break;
                case 3: spiceLevelText = "Spicy"; break;
                case 4: spiceLevelText = "Very Spicy"; break;
                case 5: spiceLevelText = "Extra Hot"; break;
                default: spiceLevelText = "Mild"; break;
            }
        }
    }
}
