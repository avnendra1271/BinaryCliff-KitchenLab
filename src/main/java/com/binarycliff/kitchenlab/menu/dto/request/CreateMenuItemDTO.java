package com.binarycliff.kitchenlab.menu.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a new menu item.
 * Contains validation rules for menu item creation.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuItemDTO {

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 200, message = "Item name must be between 2 and 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Digits(integer = 6, fraction = 2, message = "Price must have at most 6 integer digits and 2 decimal digits")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private String imageUrl;

    private Boolean isVegetarian = false;

    private Boolean isAvailable = true;

    private Boolean isPopular = false;

    private Boolean isSignature = false;

    @Min(value = 1, message = "Display order must be at least 1")
    private Integer displayOrder;

    @Size(max = 500, message = "Allergens must not exceed 500 characters")
    private String allergens;

    @Size(max = 1000, message = "Ingredients must not exceed 1000 characters")
    private String ingredients;

    @Size(max = 200, message = "Preparation time must not exceed 200 characters")
    private String preparationTime;

    @Min(value = 0, message = "Calories must be non-negative")
    private Integer calories;

    @Min(value = 0) @Max(value = 5)
    private Integer spiceLevel;

    private Boolean customizable = false;
}
