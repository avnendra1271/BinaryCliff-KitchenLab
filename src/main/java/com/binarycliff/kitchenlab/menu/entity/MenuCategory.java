package com.binarycliff.kitchenlab.menu.entity;

import com.binarycliff.kitchenlab.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Menu category entity for organizing menu items.
 * Supports multi-tenant architecture with restaurant-specific categories.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "menu_categories", 
       indexes = {
           @Index(name = "idx_category_restaurant", columnList = "restaurantId"),
           @Index(name = "idx_category_display_order", columnList = "restaurantId, displayOrder"),
           @Index(name = "idx_category_active", columnList = "restaurantId, isActive")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MenuCategory extends BaseEntity {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @NotNull(message = "Active status is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @NotNull(message = "Restaurant ID is required")
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems;

    /**
     * Check if category is currently active and available.
     * 
     * @return true if category is active and not deleted
     */
    public boolean isCurrentlyActive() {
        return Boolean.TRUE.equals(isActive) && isActive();
    }

    /**
     * Get the count of active menu items in this category.
     * 
     * @return number of active menu items
     */
    public long getActiveMenuItemCount() {
        if (menuItems == null) {
            return 0;
        }
        return menuItems.stream()
                .filter(item -> item.isCurrentlyAvailable())
                .count();
    }

    /**
     * Add a menu item to this category.
     * 
     * @param menuItem the menu item to add
     */
    public void addMenuItem(MenuItem menuItem) {
        if (menuItems != null) {
            menuItems.add(menuItem);
        }
        menuItem.setCategory(this);
    }

    /**
     * Remove a menu item from this category.
     * 
     * @param menuItem the menu item to remove
     */
    public void removeMenuItem(MenuItem menuItem) {
        if (menuItems != null) {
            menuItems.remove(menuItem);
        }
        menuItem.setCategory(null);
    }

    @PreRemove
    protected void onPreRemove() {
        // Ensure bidirectional relationship is maintained
        if (menuItems != null) {
            menuItems.forEach(item -> item.setCategory(null));
        }
    }
}
