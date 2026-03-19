package com.binarycliff.kitchenlab.menu.service;

import com.binarycliff.kitchenlab.menu.dto.request.CreateMenuCategoryDTO;
import com.binarycliff.kitchenlab.menu.dto.request.CreateMenuItemDTO;
import com.binarycliff.kitchenlab.menu.dto.response.MenuCategoryDTO;
import com.binarycliff.kitchenlab.menu.dto.response.MenuItemDTO;
import com.binarycliff.kitchenlab.menu.dto.response.MenuStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for menu management operations.
 * Provides comprehensive CRUD operations and business logic for menu categories and items.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
public interface MenuService {

    // ==================== Menu Category Operations ====================

    /**
     * Create a new menu category for the specified restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryDTO the category creation data
     * @return created category DTO
     */
    MenuCategoryDTO createCategory(UUID restaurantId, CreateMenuCategoryDTO categoryDTO);

    /**
     * Update an existing menu category.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryId the category ID
     * @param categoryDTO the category update data
     * @return updated category DTO
     */
    MenuCategoryDTO updateCategory(UUID restaurantId, UUID categoryId, CreateMenuCategoryDTO categoryDTO);

    /**
     * Get a menu category by ID.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryId the category ID
     * @return category DTO
     */
    MenuCategoryDTO getCategoryById(UUID restaurantId, UUID categoryId);

    /**
     * Get all active categories for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return list of active category DTOs
     */
    List<MenuCategoryDTO> getActiveCategories(UUID restaurantId);

    /**
     * Get all categories for a restaurant (including inactive).
     * 
     * @param restaurantId the restaurant ID
     * @return list of all category DTOs
     */
    List<MenuCategoryDTO> getAllCategories(UUID restaurantId);

    /**
     * Delete (soft delete) a menu category.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryId the category ID
     */
    void deleteCategory(UUID restaurantId, UUID categoryId);

    /**
     * Toggle category active status.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryId the category ID
     * @return updated category DTO
     */
    MenuCategoryDTO toggleCategoryStatus(UUID restaurantId, UUID categoryId);

    /**
     * Search categories by name or description.
     * 
     * @param restaurantId the restaurant ID
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of matching categories
     */
    Page<MenuCategoryDTO> searchCategories(UUID restaurantId, String searchTerm, Pageable pageable);

    // ==================== Menu Item Operations ====================

    /**
     * Create a new menu item for the specified restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @param itemDTO the item creation data
     * @return created menu item DTO
     */
    MenuItemDTO createMenuItem(UUID restaurantId, CreateMenuItemDTO itemDTO);

    /**
     * Update an existing menu item.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     * @param itemDTO the item update data
     * @return updated menu item DTO
     */
    MenuItemDTO updateMenuItem(UUID restaurantId, UUID itemId, CreateMenuItemDTO itemDTO);

    /**
     * Get a menu item by ID.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     * @return menu item DTO
     */
    MenuItemDTO getMenuItemById(UUID restaurantId, UUID itemId);

    /**
     * Get all active menu items for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return list of active menu item DTOs
     */
    List<MenuItemDTO> getActiveMenuItems(UUID restaurantId);

    /**
     * Get menu items by category for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryId the category ID
     * @return list of menu item DTOs in the category
     */
    List<MenuItemDTO> getMenuItemByCategory(UUID restaurantId, UUID categoryId);

    /**
     * Get popular menu items for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @param limit maximum number of items to return
     * @return list of popular menu item DTOs
     */
    List<MenuItemDTO> getPopularMenuItems(UUID restaurantId, int limit);

    /**
     * Get menu items on sale for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return list of menu item DTOs with discounts
     */
    List<MenuItemDTO> getOnSaleMenuItems(UUID restaurantId);

    /**
     * Get vegetarian menu items for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return list of vegetarian menu item DTOs
     */
    List<MenuItemDTO> getVegetarianMenuItems(UUID restaurantId);

    /**
     * Delete (soft delete) a menu item.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     */
    void deleteMenuItem(UUID restaurantId, UUID itemId);

    /**
     * Toggle menu item availability status.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     * @return updated menu item DTO
     */
    MenuItemDTO toggleMenuItemAvailability(UUID restaurantId, UUID itemId);

    /**
     * Toggle menu item popularity status.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     * @return updated menu item DTO
     */
    MenuItemDTO toggleMenuItemPopularity(UUID restaurantId, UUID itemId);

    /**
     * Search menu items by name, description, or ingredients.
     * 
     * @param restaurantId the restaurant ID
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of matching menu items
     */
    Page<MenuItemDTO> searchMenuItems(UUID restaurantId, String searchTerm, Pageable pageable);

    /**
     * Increment menu item view count for analytics.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     */
    void incrementMenuItemViewCount(UUID restaurantId, UUID itemId);

    /**
     * Increment menu item order count for analytics.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     */
    void incrementMenuItemOrderCount(UUID restaurantId, UUID itemId);

    // ==================== Menu Summary Operations ====================

    /**
     * Get complete menu structure for a restaurant (categories with items).
     * 
     * @param restaurantId the restaurant ID
     * @return list of category DTOs with nested menu items
     */
    List<MenuCategoryDTO> getCompleteMenu(UUID restaurantId);

    /**
     * Get menu statistics for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return menu statistics
     */
    MenuStatistics getMenuStatistics(UUID restaurantId);
}
