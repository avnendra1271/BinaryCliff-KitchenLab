package com.binarycliff.kitchenlab.menu.repository;

import com.binarycliff.kitchenlab.menu.entity.MenuCategory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MenuCategory entity operations.
 * Provides comprehensive CRUD operations and business-specific queries.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID>, JpaSpecificationExecutor<MenuCategory> {

    /**
     * Find all active categories for a specific restaurant ordered by display order.
     * 
     * @param restaurantId the restaurant ID
     * @return list of active categories ordered by display order
     */
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId AND mc.isDeleted = false AND mc.isActive = true ORDER BY mc.displayOrder ASC")
    List<MenuCategory> findActiveByRestaurantIdOrderByDisplayOrder(@Param("restaurantId") UUID restaurantId);

    /**
     * Find all categories for a specific restaurant (including inactive).
     * 
     * @param restaurantId the restaurant ID
     * @return list of all categories for the restaurant
     */
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId AND mc.isDeleted = false ORDER BY mc.displayOrder ASC")
    List<MenuCategory> findAllByRestaurantIdOrderByDisplayOrder(@Param("restaurantId") UUID restaurantId);

    /**
     * Find category by name and restaurant.
     * 
     * @param name the category name
     * @param restaurantId the restaurant ID
     * @return optional containing the category if found
     */
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId AND mc.name = :name AND mc.isDeleted = false")
    Optional<MenuCategory> findByNameAndRestaurantId(@Param("name") String name, @Param("restaurantId") UUID restaurantId);

    /**
     * Find category by name and restaurant excluding a specific ID (for update validation).
     * 
     * @param name the category name
     * @param restaurantId the restaurant ID
     * @param excludeId the ID to exclude
     * @return optional containing the category if found
     */
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId AND mc.name = :name AND mc.id != :excludeId AND mc.isDeleted = false")
    Optional<MenuCategory> findByNameAndRestaurantIdExcludingId(@Param("name") String name, @Param("restaurantId") UUID restaurantId, @Param("excludeId") UUID excludeId);

    /**
     * Find category by display order and restaurant.
     * 
     * @param displayOrder the display order
     * @param restaurantId the restaurant ID
     * @return optional containing the category if found
     */
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId AND mc.displayOrder = :displayOrder AND mc.isDeleted = false")
    Optional<MenuCategory> findByDisplayOrderAndRestaurantId(@Param("displayOrder") Integer displayOrder, @Param("restaurantId") UUID restaurantId);

    /**
     * Get the maximum display order for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return the maximum display order or 0 if no categories exist
     */
    @Query("SELECT COALESCE(MAX(mc.displayOrder), 0) FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId AND mc.isDeleted = false")
    Integer findMaxDisplayOrderByRestaurantId(@Param("restaurantId") UUID restaurantId);

    /**
     * Check if category name exists for a restaurant.
     * 
     * @param name the category name
     * @param restaurantId the restaurant ID
     * @return true if category exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(mc) > 0 THEN true ELSE false END FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId AND mc.name = :name AND mc.isDeleted = false")
    boolean existsByNameAndRestaurantId(@Param("name") String name, @Param("restaurantId") UUID restaurantId);

    /**
     * Get count of active categories for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return count of active categories
     */
    @Query("SELECT COUNT(mc) FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId AND mc.isDeleted = false AND mc.isActive = true")
    long countActiveByRestaurantId(@Param("restaurantId") UUID restaurantId);

    /**
     * Find categories with pagination for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @param pageable the pagination information
     * @return page of categories
     */
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId AND mc.isDeleted = false ORDER BY mc.displayOrder ASC")
    Page<MenuCategory> findByRestaurantId(@Param("restaurantId") UUID restaurantId, Pageable pageable);

    /**
     * Search categories by name for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @param searchTerm the search term
     * @param pageable the pagination information
     * @return page of matching categories
     */
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId AND mc.isDeleted = false AND (LOWER(mc.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(mc.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY mc.displayOrder ASC")
    Page<MenuCategory> searchByRestaurantId(@Param("restaurantId") UUID restaurantId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Get categories with item counts for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return list of categories with their active item counts
     */
    @Query("SELECT DISTINCT mc FROM MenuCategory mc LEFT JOIN mc.menuItems mi WHERE mc.restaurantId = :restaurantId AND mc.isDeleted = false ORDER BY mc.displayOrder ASC")
    List<MenuCategory> findWithMenuItemCountsByRestaurantId(@Param("restaurantId") UUID restaurantId);
}
