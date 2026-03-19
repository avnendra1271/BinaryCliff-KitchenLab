package com.binarycliff.kitchenlab.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for menu category response.
 * Provides category information with item counts.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryDTO {

    private UUID id;
    private String name;
    private String description;
    private Integer displayOrder;
    private Boolean isActive;
    private String imageUrl;
    private Long activeMenuItemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested menu items (optional, for detailed view)
    private List<MenuItemDTO> menuItems;

    /**
     * Create a simple category DTO without menu items.
     * 
     * @param id the category ID
     * @param name the category name
     * @param description the category description
     * @param displayOrder the display order
     * @param isActive the active status
     * @param imageUrl the image URL
     * @param activeMenuItemCount the count of active menu items
     * @param createdAt the creation timestamp
     * @param updatedAt the last update timestamp
     * @return MenuCategoryDTO instance
     */
    public static MenuCategoryDTO createSimple(UUID id, String name, String description, 
                                               Integer displayOrder, Boolean isActive, 
                                               String imageUrl, Long activeMenuItemCount,
                                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        return MenuCategoryDTO.builder()
                .id(id)
                .name(name)
                .description(description)
                .displayOrder(displayOrder)
                .isActive(isActive)
                .imageUrl(imageUrl)
                .activeMenuItemCount(activeMenuItemCount)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
