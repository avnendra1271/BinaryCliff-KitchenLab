package com.binarycliff.kitchenlab.menu.repository;

import com.binarycliff.kitchenlab.menu.entity.MenuItem;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MenuItem entity operations.
 * Provides comprehensive CRUD operations and business-specific queries.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID>, JpaSpecificationExecutor<MenuItem> {

    /**
     * Find all active menu items for a specific restaurant ordered by category and display order.
     * 
     * @param restaurantId the restaurant ID
     * @return list of active menu items
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isDeleted = false AND mi.isAvailable = true ORDER BY mi.category.displayOrder ASC, mi.displayOrder ASC")
    List<MenuItem> findActiveByRestaurantIdOrderByCategoryAndDisplayOrder(@Param("restaurantId") UUID restaurantId);

    /**
     * Find all menu items for a specific restaurant (including inactive).
     * 
     * @param restaurantId the restaurant ID
     * @return list of all menu items for the restaurant
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isDeleted = false ORDER BY mi.category.displayOrder ASC, mi.displayOrder ASC")
    List<MenuItem> findAllByRestaurantIdOrderByCategoryAndDisplayOrder(@Param("restaurantId") UUID restaurantId);

    /**
     * Find menu items by category for a restaurant.
     * 
     * @param categoryId the category ID
     * @param restaurantId the restaurant ID
     * @return list of menu items in the category
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.category.id = :categoryId AND mi.restaurantId = :restaurantId AND mi.isDeleted = false ORDER BY mi.displayOrder ASC")
    List<MenuItem> findByCategoryIdAndRestaurantIdOrderByDisplayOrder(@Param("categoryId") UUID categoryId, @Param("restaurantId") UUID restaurantId);

    /**
     * Find active menu items by category for a restaurant.
     * 
     * @param categoryId the category ID
     * @param restaurantId the restaurant ID
     * @return list of active menu items in the category
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.category.id = :categoryId AND mi.restaurantId = :restaurantId AND mi.isDeleted = false AND mi.isAvailable = true ORDER BY mi.displayOrder ASC")
    List<MenuItem> findActiveByCategoryIdAndRestaurantIdOrderByDisplayOrder(@Param("categoryId") UUID categoryId, @Param("restaurantId") UUID restaurantId);

    /**
     * Find menu item by name and restaurant.
     * 
     * @param name the menu item name
     * @param restaurantId the restaurant ID
     * @return optional containing the menu item if found
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.name = :name AND mi.isDeleted = false")
    Optional<MenuItem> findByNameAndRestaurantId(@Param("name") String name, @Param("restaurantId") UUID restaurantId);

    /**
     * Find menu item by name and restaurant excluding a specific ID (for update validation).
     * 
     * @param name the menu item name
     * @param restaurantId the restaurant ID
     * @param excludeId the ID to exclude
     * @return optional containing the menu item if found
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.name = :name AND mi.id != :excludeId AND mi.isDeleted = false")
    Optional<MenuItem> findByNameAndRestaurantIdExcludingId(@Param("name") String name, @Param("restaurantId") UUID restaurantId, @Param("excludeId") UUID excludeId);

    /**
     * Find popular menu items for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @param limit the maximum number of items to return
     * @return list of popular menu items
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isDeleted = false AND mi.isPopular = true AND mi.isAvailable = true ORDER BY mi.orderCount DESC, mi.viewCount DESC")
    List<MenuItem> findPopularByRestaurantId(@Param("restaurantId") UUID restaurantId, Pageable limit);

    /**
     * Find menu items on sale (with discount) for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return list of menu items with discounts
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isDeleted = false AND mi.originalPrice IS NOT NULL AND mi.originalPrice > mi.price AND mi.isAvailable = true ORDER BY mi.orderCount DESC")
    List<MenuItem> findOnSaleByRestaurantId(@Param("restaurantId") UUID restaurantId);

    /**
     * Find vegetarian menu items for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return list of vegetarian menu items
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isDeleted = false AND mi.isVegetarian = true AND mi.isAvailable = true ORDER BY mi.category.displayOrder ASC, mi.displayOrder ASC")
    List<MenuItem> findVegetarianByRestaurantId(@Param("restaurantId") UUID restaurantId);

    /**
     * Find menu items within a price range for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @param minPrice the minimum price
     * @param maxPrice the maximum price
     * @return list of menu items within the price range
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isDeleted = false AND mi.price BETWEEN :minPrice AND :maxPrice AND mi.isAvailable = true ORDER BY mi.price ASC")
    List<MenuItem> findByPriceRangeAndRestaurantId(@Param("restaurantId") UUID restaurantId, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Search menu items by name, description, or ingredients for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @param searchTerm the search term
     * @param pageable the pagination information
     * @return page of matching menu items
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isDeleted = false AND (LOWER(mi.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(mi.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(mi.ingredients) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY mi.category.displayOrder ASC, mi.displayOrder ASC")
    Page<MenuItem> searchByRestaurantId(@Param("restaurantId") UUID restaurantId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Get the maximum display order for a category in a restaurant.
     * 
     * @param categoryId the category ID
     * @param restaurantId the restaurant ID
     * @return the maximum display order or 0 if no items exist
     */
    @Query("SELECT COALESCE(MAX(mi.displayOrder), 0) FROM MenuItem mi WHERE mi.category.id = :categoryId AND mi.restaurantId = :restaurantId AND mi.isDeleted = false")
    Integer findMaxDisplayOrderForCategoryAndRestaurantId(@Param("categoryId") UUID categoryId, @Param("restaurantId") UUID restaurantId);

    /**
     * Check if menu item name exists for a restaurant.
     * 
     * @param name the menu item name
     * @param restaurantId the restaurant ID
     * @return true if menu item exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(mi) > 0 THEN true ELSE false END FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.name = :name AND mi.isDeleted = false")
    boolean existsByNameAndRestaurantId(@Param("name") String name, @Param("restaurantId") UUID restaurantId);

    /**
     * Get count of active menu items for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return count of active menu items
     */
    @Query("SELECT COUNT(mi) FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isDeleted = false AND mi.isAvailable = true")
    long countActiveByRestaurantId(@Param("restaurantId") UUID restaurantId);

    /**
     * Get count of active menu items for a category.
     * 
     * @param categoryId the category ID
     * @param restaurantId the restaurant ID
     * @return count of active menu items in the category
     */
    @Query("SELECT COUNT(mi) FROM MenuItem mi WHERE mi.category.id = :categoryId AND mi.restaurantId = :restaurantId AND mi.isDeleted = false AND mi.isAvailable = true")
    long countActiveByCategoryIdAndRestaurantId(@Param("categoryId") UUID categoryId, @Param("restaurantId") UUID restaurantId);

    /**
     * Find menu items with pagination for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @param pageable the pagination information
     * @return page of menu items
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isDeleted = false ORDER BY mi.category.displayOrder ASC, mi.displayOrder ASC")
    Page<MenuItem> findByRestaurantId(@Param("restaurantId") UUID restaurantId, Pageable pageable);

    /**
     * Find menu items by spice level for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @param spiceLevel the spice level
     * @return list of menu items with the specified spice level
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isDeleted = false AND mi.spiceLevel = :spiceLevel AND mi.isAvailable = true ORDER BY mi.category.displayOrder ASC, mi.displayOrder ASC")
    List<MenuItem> findBySpiceLevelAndRestaurantId(@Param("spiceLevel") Integer spiceLevel, @Param("restaurantId") UUID restaurantId);

    /**
     * Find menu items with calories information for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return list of menu items with calories information
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isDeleted = false AND mi.calories IS NOT NULL AND mi.isAvailable = true ORDER BY mi.calories ASC")
    List<MenuItem> findWithCaloriesByRestaurantId(@Param("restaurantId") UUID restaurantId);

    /**
     * Get top selling menu items for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @param limit the maximum number of items to return
     * @return list of top selling menu items
     */
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isDeleted = false AND mi.isAvailable = true ORDER BY mi.orderCount DESC")
    List<MenuItem> findTopSellingByRestaurantId(@Param("restaurantId") UUID restaurantId, Pageable limit);
}
