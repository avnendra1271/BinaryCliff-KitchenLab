package com.binarycliff.kitchenlab.menu.service.impl;

import com.binarycliff.kitchenlab.menu.dto.request.CreateMenuCategoryDTO;
import com.binarycliff.kitchenlab.menu.dto.request.CreateMenuItemDTO;
import com.binarycliff.kitchenlab.menu.dto.response.MenuCategoryDTO;
import com.binarycliff.kitchenlab.menu.dto.response.MenuItemDTO;
import com.binarycliff.kitchenlab.menu.dto.response.MenuStatistics;
import com.binarycliff.kitchenlab.menu.entity.MenuCategory;
import com.binarycliff.kitchenlab.menu.entity.MenuItem;
import com.binarycliff.kitchenlab.menu.exception.*;
import com.binarycliff.kitchenlab.menu.repository.MenuCategoryRepository;
import com.binarycliff.kitchenlab.menu.repository.MenuItemRepository;
import com.binarycliff.kitchenlab.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of MenuService with comprehensive business logic.
 * Provides industry-level validation, error handling, and analytics support.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MenuServiceImpl implements MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;

    // ==================== Menu Category Operations ====================

    @Override
    public MenuCategoryDTO createCategory(UUID restaurantId, CreateMenuCategoryDTO categoryDTO) {
        log.info("Creating menu category '{}' for restaurant {}", categoryDTO.getName(), restaurantId);

        // Validate restaurant context
        validateRestaurantContext(restaurantId);

        // Check for duplicate category name
        if (categoryRepository.existsByNameAndRestaurantId(categoryDTO.getName(), restaurantId)) {
            throw new DuplicateMenuItemException("MenuCategory", "name", categoryDTO.getName());
        }

        // Set display order if not provided
        Integer displayOrder = categoryDTO.getDisplayOrder();
        if (displayOrder == null) {
            displayOrder = categoryRepository.findMaxDisplayOrderByRestaurantId(restaurantId) + 1;
        }
        final Integer finalDisplayOrder = displayOrder;
        
        // Check if display order is already taken
        categoryRepository.findByDisplayOrderAndRestaurantId(displayOrder, restaurantId)
                .ifPresent(existing -> {
                    throw new InvalidMenuDataException("displayOrder", finalDisplayOrder, 
                            "Display order is already in use by another category");
                });

        // Create and save category
        MenuCategory category = MenuCategory.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .displayOrder(displayOrder)
                .imageUrl(categoryDTO.getImageUrl())
                .isActive(true)
                .restaurantId(restaurantId)
                .build();

        MenuCategory savedCategory = categoryRepository.save(category);
        log.info("Successfully created menu category with ID: {}", savedCategory.getId());

        return convertToCategoryDTO(savedCategory);
    }

    @Override
    public MenuCategoryDTO updateCategory(UUID restaurantId, UUID categoryId, CreateMenuCategoryDTO categoryDTO) {
        log.info("Updating menu category {} for restaurant {}", categoryId, restaurantId);

        MenuCategory category = getCategoryEntity(restaurantId, categoryId);

        // Check for duplicate name (excluding current category)
        categoryRepository.findByNameAndRestaurantIdExcludingId(categoryDTO.getName(), restaurantId, categoryId)
                .ifPresent(existing -> {
                    throw new DuplicateMenuItemException("MenuCategory", "name", categoryDTO.getName());
                });

        // Update display order if changed
        if (categoryDTO.getDisplayOrder() != null && !categoryDTO.getDisplayOrder().equals(category.getDisplayOrder())) {
            categoryRepository.findByDisplayOrderAndRestaurantId(categoryDTO.getDisplayOrder(), restaurantId)
                    .ifPresent(existing -> {
                        throw new InvalidMenuDataException("displayOrder", categoryDTO.getDisplayOrder(), 
                                "Display order is already in use by another category");
                    });
            category.setDisplayOrder(categoryDTO.getDisplayOrder());
        }

        // Update fields
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setImageUrl(categoryDTO.getImageUrl());

        MenuCategory savedCategory = categoryRepository.save(category);
        log.info("Successfully updated menu category {}", categoryId);

        return convertToCategoryDTO(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuCategoryDTO getCategoryById(UUID restaurantId, UUID categoryId) {
        MenuCategory category = getCategoryEntity(restaurantId, categoryId);
        return convertToCategoryDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuCategoryDTO> getActiveCategories(UUID restaurantId) {
        log.debug("Fetching active categories for restaurant {}", restaurantId);
        
        List<MenuCategory> categories = categoryRepository.findActiveByRestaurantIdOrderByDisplayOrder(restaurantId);
        return categories.stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuCategoryDTO> getAllCategories(UUID restaurantId) {
        log.debug("Fetching all categories for restaurant {}", restaurantId);
        
        List<MenuCategory> categories = categoryRepository.findAllByRestaurantIdOrderByDisplayOrder(restaurantId);
        return categories.stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCategory(UUID restaurantId, UUID categoryId) {
        log.info("Deleting menu category {} for restaurant {}", categoryId, restaurantId);

        MenuCategory category = getCategoryEntity(restaurantId, categoryId);

        // Check if category has active menu items
        long activeItemCount = itemRepository.countActiveByCategoryIdAndRestaurantId(categoryId, restaurantId);
        if (activeItemCount > 0) {
            throw new InvalidMenuDataException(String.format(
                    "Cannot delete category with %d active menu items. Please deactivate or move the items first.", 
                    activeItemCount));
        }

        category.softDelete();
        categoryRepository.save(category);
        log.info("Successfully deleted menu category {}", categoryId);
    }

    @Override
    public MenuCategoryDTO toggleCategoryStatus(UUID restaurantId, UUID categoryId) {
        log.info("Toggling status for menu category {} in restaurant {}", categoryId, restaurantId);

        MenuCategory category = getCategoryEntity(restaurantId, categoryId);
        category.setIsActive(!category.getIsActive());
        
        MenuCategory savedCategory = categoryRepository.save(category);
        log.info("Category {} status toggled to: {}", categoryId, savedCategory.getIsActive());

        return convertToCategoryDTO(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MenuCategoryDTO> searchCategories(UUID restaurantId, String searchTerm, Pageable pageable) {
        log.debug("Searching categories for restaurant {} with term: {}", restaurantId, searchTerm);
        
        Page<MenuCategory> categories = categoryRepository.searchByRestaurantId(restaurantId, searchTerm, pageable);
        return categories.map(this::convertToCategoryDTO);
    }

    // ==================== Menu Item Operations ====================

    @Override
    public MenuItemDTO createMenuItem(UUID restaurantId, CreateMenuItemDTO itemDTO) {
        log.info("Creating menu item '{}' for restaurant {}", itemDTO.getName(), restaurantId);

        // Validate restaurant context
        validateRestaurantContext(restaurantId);

        // Get and validate category
        MenuCategory category = getCategoryEntity(restaurantId, itemDTO.getCategoryId());
        if (!category.isCurrentlyActive()) {
            throw new InvalidMenuDataException("Cannot add menu item to inactive category: " + category.getName());
        }

        // Check for duplicate item name
        if (itemRepository.existsByNameAndRestaurantId(itemDTO.getName(), restaurantId)) {
            throw new DuplicateMenuItemException("MenuItem", "name", itemDTO.getName());
        }

        // Validate price
        if (itemDTO.getOriginalPrice() != null && itemDTO.getOriginalPrice().compareTo(itemDTO.getPrice()) < 0) {
            throw new InvalidMenuDataException("originalPrice", itemDTO.getOriginalPrice(), 
                    "Original price cannot be less than current price");
        }

        // Set display order if not provided
        Integer displayOrder = itemDTO.getDisplayOrder();
        if (displayOrder == null) {
            displayOrder = itemRepository.findMaxDisplayOrderForCategoryAndRestaurantId(
                    itemDTO.getCategoryId(), restaurantId) + 1;
        }

        // Create and save menu item
        MenuItem menuItem = MenuItem.builder()
                .name(itemDTO.getName())
                .description(itemDTO.getDescription())
                .category(category)
                .price(itemDTO.getPrice())
                .originalPrice(itemDTO.getOriginalPrice())
                .imageUrl(itemDTO.getImageUrl())
                .isVegetarian(itemDTO.getIsVegetarian() != null ? itemDTO.getIsVegetarian() : false)
                .isAvailable(itemDTO.getIsAvailable() != null ? itemDTO.getIsAvailable() : true)
                .isPopular(itemDTO.getIsPopular() != null ? itemDTO.getIsPopular() : false)
                .isSignature(itemDTO.getIsSignature() != null ? itemDTO.getIsSignature() : false)
                .displayOrder(displayOrder)
                .allergens(itemDTO.getAllergens())
                .ingredients(itemDTO.getIngredients())
                .preparationTime(itemDTO.getPreparationTime())
                .calories(itemDTO.getCalories())
                .spiceLevel(itemDTO.getSpiceLevel())
                .customizable(itemDTO.getCustomizable() != null ? itemDTO.getCustomizable() : false)
                .orderCount(0L)
                .viewCount(0L)
                .restaurantId(restaurantId)
                .build();

        MenuItem savedItem = itemRepository.save(menuItem);
        log.info("Successfully created menu item with ID: {}", savedItem.getId());

        return convertToMenuItemDTO(savedItem);
    }

    @Override
    public MenuItemDTO updateMenuItem(UUID restaurantId, UUID itemId, CreateMenuItemDTO itemDTO) {
        log.info("Updating menu item {} for restaurant {}", itemId, restaurantId);

        MenuItem menuItem = getMenuItemEntity(restaurantId, itemId);

        // Check for duplicate name (excluding current item)
        itemRepository.findByNameAndRestaurantIdExcludingId(itemDTO.getName(), restaurantId, itemId)
                .ifPresent(existing -> {
                    throw new DuplicateMenuItemException("MenuItem", "name", itemDTO.getName());
                });

        // Validate category change
        if (!itemDTO.getCategoryId().equals(menuItem.getCategory().getId())) {
            MenuCategory newCategory = getCategoryEntity(restaurantId, itemDTO.getCategoryId());
            if (!newCategory.isCurrentlyActive()) {
                throw new InvalidMenuDataException("Cannot move menu item to inactive category: " + newCategory.getName());
            }
            menuItem.setCategory(newCategory);
        }

        // Validate price
        if (itemDTO.getOriginalPrice() != null && itemDTO.getOriginalPrice().compareTo(itemDTO.getPrice()) < 0) {
            throw new InvalidMenuDataException("originalPrice", itemDTO.getOriginalPrice(), 
                    "Original price cannot be less than current price");
        }

        // Update fields
        menuItem.setName(itemDTO.getName());
        menuItem.setDescription(itemDTO.getDescription());
        menuItem.setPrice(itemDTO.getPrice());
        menuItem.setOriginalPrice(itemDTO.getOriginalPrice());
        menuItem.setImageUrl(itemDTO.getImageUrl());
        menuItem.setIsVegetarian(itemDTO.getIsVegetarian());
        menuItem.setIsAvailable(itemDTO.getIsAvailable());
        menuItem.setIsPopular(itemDTO.getIsPopular());
        menuItem.setIsSignature(itemDTO.getIsSignature());
        menuItem.setAllergens(itemDTO.getAllergens());
        menuItem.setIngredients(itemDTO.getIngredients());
        menuItem.setPreparationTime(itemDTO.getPreparationTime());
        menuItem.setCalories(itemDTO.getCalories());
        menuItem.setSpiceLevel(itemDTO.getSpiceLevel());
        menuItem.setCustomizable(itemDTO.getCustomizable());

        MenuItem savedItem = itemRepository.save(menuItem);
        log.info("Successfully updated menu item {}", itemId);

        return convertToMenuItemDTO(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemDTO getMenuItemById(UUID restaurantId, UUID itemId) {
        MenuItem menuItem = getMenuItemEntity(restaurantId, itemId);
        return convertToMenuItemDTO(menuItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDTO> getActiveMenuItems(UUID restaurantId) {
        log.debug("Fetching active menu items for restaurant {}", restaurantId);
        
        List<MenuItem> items = itemRepository.findActiveByRestaurantIdOrderByCategoryAndDisplayOrder(restaurantId);
        return items.stream()
                .map(this::convertToMenuItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDTO> getMenuItemByCategory(UUID restaurantId, UUID categoryId) {
        log.debug("Fetching menu items for category {} in restaurant {}", categoryId, restaurantId);
        
        // Validate category exists
        getCategoryEntity(restaurantId, categoryId);
        
        List<MenuItem> items = itemRepository.findActiveByCategoryIdAndRestaurantIdOrderByDisplayOrder(
                categoryId, restaurantId);
        return items.stream()
                .map(this::convertToMenuItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDTO> getPopularMenuItems(UUID restaurantId, int limit) {
        log.debug("Fetching {} popular menu items for restaurant {}", limit, restaurantId);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<MenuItem> items = itemRepository.findPopularByRestaurantId(restaurantId, pageable);
        return items.stream()
                .map(this::convertToMenuItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDTO> getOnSaleMenuItems(UUID restaurantId) {
        log.debug("Fetching menu items on sale for restaurant {}", restaurantId);
        
        List<MenuItem> items = itemRepository.findOnSaleByRestaurantId(restaurantId);
        return items.stream()
                .map(this::convertToMenuItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDTO> getVegetarianMenuItems(UUID restaurantId) {
        log.debug("Fetching vegetarian menu items for restaurant {}", restaurantId);
        
        List<MenuItem> items = itemRepository.findVegetarianByRestaurantId(restaurantId);
        return items.stream()
                .map(this::convertToMenuItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMenuItem(UUID restaurantId, UUID itemId) {
        log.info("Deleting menu item {} for restaurant {}", itemId, restaurantId);

        MenuItem menuItem = getMenuItemEntity(restaurantId, itemId);
        menuItem.softDelete();
        
        itemRepository.save(menuItem);
        log.info("Successfully deleted menu item {}", itemId);
    }

    @Override
    public MenuItemDTO toggleMenuItemAvailability(UUID restaurantId, UUID itemId) {
        log.info("Toggling availability for menu item {} in restaurant {}", itemId, restaurantId);

        MenuItem menuItem = getMenuItemEntity(restaurantId, itemId);
        menuItem.setIsAvailable(!menuItem.getIsAvailable());
        
        MenuItem savedItem = itemRepository.save(menuItem);
        log.info("Menu item {} availability toggled to: {}", itemId, savedItem.getIsAvailable());

        return convertToMenuItemDTO(savedItem);
    }

    @Override
    public MenuItemDTO toggleMenuItemPopularity(UUID restaurantId, UUID itemId) {
        log.info("Toggling popularity for menu item {} in restaurant {}", itemId, restaurantId);

        MenuItem menuItem = getMenuItemEntity(restaurantId, itemId);
        menuItem.setIsPopular(!menuItem.getIsPopular());
        
        MenuItem savedItem = itemRepository.save(menuItem);
        log.info("Menu item {} popularity toggled to: {}", itemId, savedItem.getIsPopular());

        return convertToMenuItemDTO(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MenuItemDTO> searchMenuItems(UUID restaurantId, String searchTerm, Pageable pageable) {
        log.debug("Searching menu items for restaurant {} with term: {}", restaurantId, searchTerm);
        
        Page<MenuItem> items = itemRepository.searchByRestaurantId(restaurantId, searchTerm, pageable);
        return items.map(this::convertToMenuItemDTO);
    }

    @Override
    public void incrementMenuItemViewCount(UUID restaurantId, UUID itemId) {
        MenuItem menuItem = getMenuItemEntity(restaurantId, itemId);
        menuItem.incrementViewCount();
        itemRepository.save(menuItem);
    }

    @Override
    public void incrementMenuItemOrderCount(UUID restaurantId, UUID itemId) {
        MenuItem menuItem = getMenuItemEntity(restaurantId, itemId);
        menuItem.incrementOrderCount();
        itemRepository.save(menuItem);
    }

    // ==================== Menu Summary Operations ====================

    @Override
    @Transactional(readOnly = true)
    public List<MenuCategoryDTO> getCompleteMenu(UUID restaurantId) {
        log.debug("Fetching complete menu for restaurant {}", restaurantId);
        
        List<MenuCategory> categories = categoryRepository.findActiveByRestaurantIdOrderByDisplayOrder(restaurantId);
        
        return categories.stream()
                .map(category -> {
                    List<MenuItem> items = itemRepository.findActiveByCategoryIdAndRestaurantIdOrderByDisplayOrder(
                            category.getId(), restaurantId);
                    List<MenuItemDTO> itemDTOs = items.stream()
                            .map(this::convertToMenuItemDTO)
                            .collect(Collectors.toList());
                    
                    return MenuCategoryDTO.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .description(category.getDescription())
                            .displayOrder(category.getDisplayOrder())
                            .isActive(category.getIsActive())
                            .imageUrl(category.getImageUrl())
                            .activeMenuItemCount((long) itemDTOs.size())
                            .createdAt(category.getCreatedAt())
                            .updatedAt(category.getUpdatedAt())
                            .menuItems(itemDTOs)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MenuStatistics getMenuStatistics(UUID restaurantId) {
        log.debug("Calculating menu statistics for restaurant {}", restaurantId);
        
        // Category statistics
        long totalCategories = categoryRepository.count();
        long activeCategories = categoryRepository.countActiveByRestaurantId(restaurantId);
        
        // Item statistics
        long totalMenuItems = itemRepository.count();
        long activeMenuItems = itemRepository.countActiveByRestaurantId(restaurantId);
        long vegetarianItems = itemRepository.findVegetarianByRestaurantId(restaurantId).size();
        long itemsOnSale = itemRepository.findOnSaleByRestaurantId(restaurantId).size();
        
        // Popular items
        List<MenuItem> popularItems = itemRepository.findPopularByRestaurantId(restaurantId, PageRequest.of(0, 1));
        long popularItemsCount = itemRepository.findPopularByRestaurantId(restaurantId, PageRequest.of(0, 100)).size();
        
        // Calculate averages
        List<MenuItem> allActiveItems = itemRepository.findActiveByRestaurantIdOrderByCategoryAndDisplayOrder(restaurantId);
        Double averagePrice = allActiveItems.stream()
                .mapToDouble(item -> item.getPrice().doubleValue())
                .average()
                .orElse(0.0);
        
        Double averageCalories = allActiveItems.stream()
                .filter(item -> item.getCalories() != null)
                .mapToInt(MenuItem::getCalories)
                .average()
                .orElse(0.0);
        
        // Total analytics
        Long totalViews = allActiveItems.stream()
                .mapToLong(item -> item.getViewCount() != null ? item.getViewCount() : 0)
                .sum();
        
        Long totalOrders = allActiveItems.stream()
                .mapToLong(item -> item.getOrderCount() != null ? item.getOrderCount() : 0)
                .sum();
        
        // Most popular category and item
        MenuCategory mostPopularCategory = categoryRepository.findActiveByRestaurantIdOrderByDisplayOrder(restaurantId)
                .stream()
                .max((c1, c2) -> Long.compare(c2.getActiveMenuItemCount(), c1.getActiveMenuItemCount()))
                .orElse(null);
        
        MenuItem mostPopularItem = allActiveItems.stream()
                .max((i1, i2) -> Long.compare(
                        i2.getOrderCount() != null ? i2.getOrderCount() : 0,
                        i1.getOrderCount() != null ? i1.getOrderCount() : 0))
                .orElse(null);
        
        return MenuStatistics.builder()
                .totalCategories(totalCategories)
                .activeCategories(activeCategories)
                .totalMenuItems(totalMenuItems)
                .activeMenuItems(activeMenuItems)
                .vegetarianItems(vegetarianItems)
                .popularItems(popularItemsCount)
                .itemsOnSale(itemsOnSale)
                .averagePrice(averagePrice)
                .averageCalories(averageCalories)
                .totalViews(totalViews)
                .totalOrders(totalOrders)
                .mostPopularCategoryId(mostPopularCategory != null ? mostPopularCategory.getId() : null)
                .mostPopularCategoryName(mostPopularCategory != null ? mostPopularCategory.getName() : null)
                .mostPopularItemId(mostPopularItem != null ? mostPopularItem.getId() : null)
                .mostPopularItemName(mostPopularItem != null ? mostPopularItem.getName() : null)
                .build();
    }

    // ==================== Helper Methods ====================

    private void validateRestaurantContext(UUID restaurantId) {
        // In a real implementation, you would validate that the restaurant exists
        // and the current user has permission to manage menus for this restaurant
        log.debug("Validating restaurant context for {}", restaurantId);
    }

    private MenuCategory getCategoryEntity(UUID restaurantId, UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .filter(category -> category.getRestaurantId().equals(restaurantId))
                .filter(MenuCategory::isActive)
                .orElseThrow(() -> new MenuNotFoundException("MenuCategory", "id", categoryId));
    }

    private MenuItem getMenuItemEntity(UUID restaurantId, UUID itemId) {
        return itemRepository.findById(itemId)
                .filter(item -> item.getRestaurantId().equals(restaurantId))
                .filter(MenuItem::isActive)
                .orElseThrow(() -> new MenuNotFoundException("MenuItem", "id", itemId));
    }

    private MenuCategoryDTO convertToCategoryDTO(MenuCategory category) {
        long activeItemCount = itemRepository.countActiveByCategoryIdAndRestaurantId(
                category.getId(), category.getRestaurantId());
        
        return MenuCategoryDTO.createSimple(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getDisplayOrder(),
                category.getIsActive(),
                category.getImageUrl(),
                activeItemCount,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private MenuItemDTO convertToMenuItemDTO(MenuItem item) {
        return MenuItemDTO.createSimple(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getPrice(),
                item.getOriginalPrice(),
                item.getImageUrl(),
                item.getIsVegetarian(),
                item.getIsAvailable(),
                item.getIsPopular(),
                item.getIsSignature(),
                item.getDisplayOrder(),
                item.getSpiceLevel()
        );
    }
}
