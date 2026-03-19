package com.binarycliff.kitchenlab.menu.controller;

import com.binarycliff.kitchenlab.menu.dto.request.CreateMenuCategoryDTO;
import com.binarycliff.kitchenlab.menu.dto.request.CreateMenuItemDTO;
import com.binarycliff.kitchenlab.menu.dto.response.MenuCategoryDTO;
import com.binarycliff.kitchenlab.menu.dto.response.MenuItemDTO;
import com.binarycliff.kitchenlab.menu.dto.response.MenuStatistics;
import com.binarycliff.kitchenlab.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for menu management operations.
 * Provides comprehensive API endpoints for menu categories and items.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MenuController {

    private final MenuService menuService;

    // ==================== Menu Category Endpoints ====================

    /**
     * Create a new menu category.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryDTO the category creation data
     * @return created category
     */
    @PostMapping("/categories")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MenuCategoryDTO> createCategory(
            @RequestParam UUID restaurantId,
            @Valid @RequestBody CreateMenuCategoryDTO categoryDTO) {
        
        log.info("REST request to create category for restaurant: {}", restaurantId);
        MenuCategoryDTO result = menuService.createCategory(restaurantId, categoryDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * Update an existing menu category.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryId the category ID
     * @param categoryDTO the category update data
     * @return updated category
     */
    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MenuCategoryDTO> updateCategory(
            @RequestParam UUID restaurantId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody CreateMenuCategoryDTO categoryDTO) {
        
        log.info("REST request to update category {} for restaurant: {}", categoryId, restaurantId);
        MenuCategoryDTO result = menuService.updateCategory(restaurantId, categoryId, categoryDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * Get a menu category by ID.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryId the category ID
     * @return category details
     */
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<MenuCategoryDTO> getCategory(
            @RequestParam UUID restaurantId,
            @PathVariable UUID categoryId) {
        
        log.info("REST request to get category {} for restaurant: {}", categoryId, restaurantId);
        MenuCategoryDTO result = menuService.getCategoryById(restaurantId, categoryId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all active categories for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return list of active categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<MenuCategoryDTO>> getActiveCategories(
            @RequestParam UUID restaurantId) {
        
        log.info("REST request to get active categories for restaurant: {}", restaurantId);
        List<MenuCategoryDTO> result = menuService.getActiveCategories(restaurantId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all categories for a restaurant (including inactive).
     * 
     * @param restaurantId the restaurant ID
     * @return list of all categories
     */
    @GetMapping("/categories/all")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<MenuCategoryDTO>> getAllCategories(
            @RequestParam UUID restaurantId) {
        
        log.info("REST request to get all categories for restaurant: {}", restaurantId);
        List<MenuCategoryDTO> result = menuService.getAllCategories(restaurantId);
        return ResponseEntity.ok(result);
    }

    /**
     * Delete a menu category.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryId the category ID
     * @return no content
     */
    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteCategory(
            @RequestParam UUID restaurantId,
            @PathVariable UUID categoryId) {
        
        log.info("REST request to delete category {} for restaurant: {}", categoryId, restaurantId);
        menuService.deleteCategory(restaurantId, categoryId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle category active status.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryId the category ID
     * @return updated category
     */
    @PatchMapping("/categories/{categoryId}/toggle-status")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MenuCategoryDTO> toggleCategoryStatus(
            @RequestParam UUID restaurantId,
            @PathVariable UUID categoryId) {
        
        log.info("REST request to toggle status for category {} in restaurant: {}", categoryId, restaurantId);
        MenuCategoryDTO result = menuService.toggleCategoryStatus(restaurantId, categoryId);
        return ResponseEntity.ok(result);
    }

    /**
     * Search categories by name or description.
     * 
     * @param restaurantId the restaurant ID
     * @param searchTerm the search term
     * @param page the page number (default: 0)
     * @param size the page size (default: 20)
     * @return page of matching categories
     */
    @GetMapping("/categories/search")
    public ResponseEntity<Page<MenuCategoryDTO>> searchCategories(
            @RequestParam UUID restaurantId,
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("REST request to search categories for restaurant {} with term: {}", restaurantId, searchTerm);
        Pageable pageable = PageRequest.of(page, size);
        Page<MenuCategoryDTO> result = menuService.searchCategories(restaurantId, searchTerm, pageable);
        return ResponseEntity.ok(result);
    }

    // ==================== Menu Item Endpoints ====================

    /**
     * Create a new menu item.
     * 
     * @param restaurantId the restaurant ID
     * @param itemDTO the item creation data
     * @return created menu item
     */
    @PostMapping("/items")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MenuItemDTO> createMenuItem(
            @RequestParam UUID restaurantId,
            @Valid @RequestBody CreateMenuItemDTO itemDTO) {
        
        log.info("REST request to create menu item for restaurant: {}", restaurantId);
        MenuItemDTO result = menuService.createMenuItem(restaurantId, itemDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * Update an existing menu item.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     * @param itemDTO the item update data
     * @return updated menu item
     */
    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MenuItemDTO> updateMenuItem(
            @RequestParam UUID restaurantId,
            @PathVariable UUID itemId,
            @Valid @RequestBody CreateMenuItemDTO itemDTO) {
        
        log.info("REST request to update menu item {} for restaurant: {}", itemId, restaurantId);
        MenuItemDTO result = menuService.updateMenuItem(restaurantId, itemId, itemDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * Get a menu item by ID.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     * @return menu item details
     */
    @GetMapping("/items/{itemId}")
    public ResponseEntity<MenuItemDTO> getMenuItem(
            @RequestParam UUID restaurantId,
            @PathVariable UUID itemId) {
        
        log.info("REST request to get menu item {} for restaurant: {}", itemId, restaurantId);
        MenuItemDTO result = menuService.getMenuItemById(restaurantId, itemId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all active menu items for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return list of active menu items
     */
    @GetMapping("/items")
    public ResponseEntity<List<MenuItemDTO>> getActiveMenuItems(
            @RequestParam UUID restaurantId) {
        
        log.info("REST request to get active menu items for restaurant: {}", restaurantId);
        List<MenuItemDTO> result = menuService.getActiveMenuItems(restaurantId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get menu items by category.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryId the category ID
     * @return list of menu items in the category
     */
    @GetMapping("/items/category/{categoryId}")
    public ResponseEntity<List<MenuItemDTO>> getMenuItemByCategory(
            @RequestParam UUID restaurantId,
            @PathVariable UUID categoryId) {
        
        log.info("REST request to get menu items for category {} in restaurant: {}", categoryId, restaurantId);
        List<MenuItemDTO> result = menuService.getMenuItemByCategory(restaurantId, categoryId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get popular menu items.
     * 
     * @param restaurantId the restaurant ID
     * @param limit maximum number of items (default: 10)
     * @return list of popular menu items
     */
    @GetMapping("/items/popular")
    public ResponseEntity<List<MenuItemDTO>> getPopularMenuItems(
            @RequestParam UUID restaurantId,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("REST request to get {} popular menu items for restaurant: {}", limit, restaurantId);
        List<MenuItemDTO> result = menuService.getPopularMenuItems(restaurantId, limit);
        return ResponseEntity.ok(result);
    }

    /**
     * Get menu items on sale.
     * 
     * @param restaurantId the restaurant ID
     * @return list of menu items with discounts
     */
    @GetMapping("/items/on-sale")
    public ResponseEntity<List<MenuItemDTO>> getOnSaleMenuItems(
            @RequestParam UUID restaurantId) {
        
        log.info("REST request to get menu items on sale for restaurant: {}", restaurantId);
        List<MenuItemDTO> result = menuService.getOnSaleMenuItems(restaurantId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get vegetarian menu items.
     * 
     * @param restaurantId the restaurant ID
     * @return list of vegetarian menu items
     */
    @GetMapping("/items/vegetarian")
    public ResponseEntity<List<MenuItemDTO>> getVegetarianMenuItems(
            @RequestParam UUID restaurantId) {
        
        log.info("REST request to get vegetarian menu items for restaurant: {}", restaurantId);
        List<MenuItemDTO> result = menuService.getVegetarianMenuItems(restaurantId);
        return ResponseEntity.ok(result);
    }

    /**
     * Delete a menu item.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     * @return no content
     */
    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteMenuItem(
            @RequestParam UUID restaurantId,
            @PathVariable UUID itemId) {
        
        log.info("REST request to delete menu item {} for restaurant: {}", itemId, restaurantId);
        menuService.deleteMenuItem(restaurantId, itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle menu item availability.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     * @return updated menu item
     */
    @PatchMapping("/items/{itemId}/toggle-availability")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MenuItemDTO> toggleMenuItemAvailability(
            @RequestParam UUID restaurantId,
            @PathVariable UUID itemId) {
        
        log.info("REST request to toggle availability for menu item {} in restaurant: {}", itemId, restaurantId);
        MenuItemDTO result = menuService.toggleMenuItemAvailability(restaurantId, itemId);
        return ResponseEntity.ok(result);
    }

    /**
     * Toggle menu item popularity.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     * @return updated menu item
     */
    @PatchMapping("/items/{itemId}/toggle-popularity")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MenuItemDTO> toggleMenuItemPopularity(
            @RequestParam UUID restaurantId,
            @PathVariable UUID itemId) {
        
        log.info("REST request to toggle popularity for menu item {} in restaurant: {}", itemId, restaurantId);
        MenuItemDTO result = menuService.toggleMenuItemPopularity(restaurantId, itemId);
        return ResponseEntity.ok(result);
    }

    /**
     * Search menu items by name, description, or ingredients.
     * 
     * @param restaurantId the restaurant ID
     * @param searchTerm the search term
     * @param page the page number (default: 0)
     * @param size the page size (default: 20)
     * @return page of matching menu items
     */
    @GetMapping("/items/search")
    public ResponseEntity<Page<MenuItemDTO>> searchMenuItems(
            @RequestParam UUID restaurantId,
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("REST request to search menu items for restaurant {} with term: {}", restaurantId, searchTerm);
        Pageable pageable = PageRequest.of(page, size);
        Page<MenuItemDTO> result = menuService.searchMenuItems(restaurantId, searchTerm, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Increment menu item view count.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     * @return no content
     */
    @PostMapping("/items/{itemId}/view")
    public ResponseEntity<Void> incrementMenuItemViewCount(
            @RequestParam UUID restaurantId,
            @PathVariable UUID itemId) {
        
        log.info("REST request to increment view count for menu item {} in restaurant: {}", itemId, restaurantId);
        menuService.incrementMenuItemViewCount(restaurantId, itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Increment menu item order count.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the menu item ID
     * @return no content
     */
    @PostMapping("/items/{itemId}/order")
    public ResponseEntity<Void> incrementMenuItemOrderCount(
            @RequestParam UUID restaurantId,
            @PathVariable UUID itemId) {
        
        log.info("REST request to increment order count for menu item {} in restaurant: {}", itemId, restaurantId);
        menuService.incrementMenuItemOrderCount(restaurantId, itemId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Menu Summary Endpoints ====================

    /**
     * Get complete menu structure for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return complete menu with categories and items
     */
    @GetMapping("/complete")
    public ResponseEntity<List<MenuCategoryDTO>> getCompleteMenu(
            @RequestParam UUID restaurantId) {
        
        log.info("REST request to get complete menu for restaurant: {}", restaurantId);
        List<MenuCategoryDTO> result = menuService.getCompleteMenu(restaurantId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get menu statistics for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return menu statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MenuStatistics> getMenuStatistics(
            @RequestParam UUID restaurantId) {
        
        log.info("REST request to get menu statistics for restaurant: {}", restaurantId);
        MenuStatistics result = menuService.getMenuStatistics(restaurantId);
        return ResponseEntity.ok(result);
    }
}
