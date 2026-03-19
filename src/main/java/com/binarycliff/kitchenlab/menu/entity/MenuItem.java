package com.binarycliff.kitchenlab.menu.entity;

import com.binarycliff.kitchenlab.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Menu item entity representing individual dishes/products.
 * Supports comprehensive menu management with pricing, availability, and dietary information.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "menu_items",
       indexes = {
           @Index(name = "idx_item_restaurant", columnList = "restaurantId"),
           @Index(name = "idx_item_category", columnList = "categoryId"),
           @Index(name = "idx_item_active", columnList = "restaurantId, isAvailable"),
           @Index(name = "idx_item_popular", columnList = "restaurantId, isPopular"),
           @Index(name = "idx_item_veg", columnList = "restaurantId, isVegetarian")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MenuItem extends BaseEntity {

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 200, message = "Item name must be between 2 and 200 characters")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", length = 1000, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_category"))
    private MenuCategory category;

    @NotNull(message = "Restaurant ID is required")
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Digits(integer = 6, fraction = 2, message = "Price must have at most 6 integer digits and 2 decimal digits")
    @Column(name = "price", nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 8, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @NotNull(message = "Vegetarian status is required")
    @Column(name = "is_vegetarian", nullable = false)
    private Boolean isVegetarian = false;

    @NotNull(message = "Availability status is required")
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "is_popular")
    private Boolean isPopular = false;

    @Column(name = "is_signature")
    private Boolean isSignature = false;

    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Size(max = 500, message = "Allergens must not exceed 500 characters")
    @Column(name = "allergens", length = 500)
    private String allergens;

    @Size(max = 1000, message = "Ingredients must not exceed 1000 characters")
    @Column(name = "ingredients", length = 1000, columnDefinition = "TEXT")
    private String ingredients;

    @Size(max = 200, message = "Preparation time must not exceed 200 characters")
    @Column(name = "preparation_time", length = 200)
    private String preparationTime;

    @Min(value = 0, message = "Calories must be non-negative")
    @Column(name = "calories")
    private Integer calories;

    @Column(name = "spice_level")
    private Integer spiceLevel; // 0-5 scale

    @Column(name = "customizable")
    private Boolean customizable = false;

    // Audit fields for analytics
    @Column(name = "order_count")
    private Long orderCount = 0L;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    /**
     * Check if item is currently available for ordering.
     * 
     * @return true if item is available, not deleted, and category is active
     */
    public boolean isCurrentlyAvailable() {
        return Boolean.TRUE.equals(isAvailable) && 
               isActive() && 
               category != null && 
               category.isCurrentlyActive();
    }

    /**
     * Get the discount percentage if original price is set.
     * 
     * @return discount percentage (0-100) or null if no discount
     */
    public Double getDiscountPercentage() {
        if (originalPrice == null || originalPrice.compareTo(price) <= 0) {
            return null;
        }
        BigDecimal discount = originalPrice.subtract(price);
        return discount.divide(originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                      .multiply(BigDecimal.valueOf(100))
                      .doubleValue();
    }

    /**
     * Check if item has a discount.
     * 
     * @return true if original price is greater than current price
     */
    public boolean hasDiscount() {
        return originalPrice != null && originalPrice.compareTo(price) > 0;
    }

    /**
     * Get formatted price display.
     * 
     * @return formatted price string
     */
    public String getFormattedPrice() {
        return String.format("$%.2f", price);
    }

    /**
     * Get formatted original price display.
     * 
     * @return formatted original price string or null if not set
     */
    public String getFormattedOriginalPrice() {
        return originalPrice != null ? String.format("$%.2f", originalPrice) : null;
    }

    /**
     * Increment order count for analytics.
     */
    public void incrementOrderCount() {
        orderCount = (orderCount == null ? 0 : orderCount) + 1;
    }

    /**
     * Increment view count for analytics.
     */
    public void incrementViewCount() {
        viewCount = (viewCount == null ? 0 : viewCount) + 1;
    }

    /**
     * Get spice level display text.
     * 
     * @return spice level text
     */
    public String getSpiceLevelText() {
        if (spiceLevel == null || spiceLevel < 0) {
            return "Mild";
        }
        switch (spiceLevel) {
            case 0: return "Mild";
            case 1: return "Mild";
            case 2: return "Medium";
            case 3: return "Spicy";
            case 4: return "Very Spicy";
            case 5: return "Extra Hot";
            default: return "Mild";
        }
    }

    /**
     * Validate business rules.
     * 
     * @throws IllegalArgumentException if business rules are violated
     */
    @PrePersist
    @PreUpdate
    protected void validateBusinessRules() {
        if (originalPrice != null && originalPrice.compareTo(price) < 0) {
            throw new IllegalArgumentException("Original price cannot be less than current price");
        }
        
        if (spiceLevel != null && (spiceLevel < 0 || spiceLevel > 5)) {
            throw new IllegalArgumentException("Spice level must be between 0 and 5");
        }
        
        if (category != null && !category.getRestaurantId().equals(this.getRestaurantId())) {
            throw new IllegalArgumentException("Menu item and category must belong to the same restaurant");
        }
    }
}
