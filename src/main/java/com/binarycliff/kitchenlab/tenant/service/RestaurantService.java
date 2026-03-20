package com.binarycliff.kitchenlab.tenant.service;

import com.binarycliff.kitchenlab.tenant.entity.Restaurant;
import com.binarycliff.kitchenlab.tenant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing restaurant tenants.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {
    
    private final RestaurantRepository restaurantRepository;
    
    /**
     * Create a new restaurant tenant.
     */
    @Transactional
    public Restaurant createRestaurant(Restaurant restaurant) {
        log.info("Creating new restaurant: {}", restaurant.getName());
        
        // Validate subdomain uniqueness
        if (restaurantRepository.existsBySubdomain(restaurant.getSubdomain())) {
            throw new IllegalArgumentException("Subdomain already exists: " + restaurant.getSubdomain());
        }
        
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());
        restaurant.setIsActive(true);
        restaurant.setIsDeleted(false);
        
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Successfully created restaurant with ID: {}", saved.getId());
        
        return saved;
    }
    
    /**
     * Update an existing restaurant.
     */
    @Transactional
    public Restaurant updateRestaurant(UUID id, Restaurant restaurantDetails) {
        log.info("Updating restaurant with ID: {}", id);
        
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + id));
        
        // Update allowed fields
        restaurant.setName(restaurantDetails.getName());
        restaurant.setDescription(restaurantDetails.getDescription());
        restaurant.setAddress(restaurantDetails.getAddress());
        restaurant.setPhone(restaurantDetails.getPhone());
        restaurant.setEmail(restaurantDetails.getEmail());
        restaurant.setLogoUrl(restaurantDetails.getLogoUrl());
        restaurant.setPrimaryColor(restaurantDetails.getPrimaryColor());
        restaurant.setSecondaryColor(restaurantDetails.getSecondaryColor());
        restaurant.setWebsite(restaurantDetails.getWebsite());
        restaurant.setCuisineType(restaurantDetails.getCuisineType());
        restaurant.setDeliveryRadiusKm(restaurantDetails.getDeliveryRadiusKm());
        restaurant.setAllowsOnlineOrders(restaurantDetails.getAllowsOnlineOrders());
        restaurant.setAllowsDineIn(restaurantDetails.getAllowsDineIn());
        restaurant.setAllowsTakeout(restaurantDetails.getAllowsTakeout());
        restaurant.setOperatingHours(restaurantDetails.getOperatingHours());
        restaurant.setDeliveryFee(restaurantDetails.getDeliveryFee());
        restaurant.setMinimumOrderAmount(restaurantDetails.getMinimumOrderAmount());
        restaurant.setPreparationTimeMinutes(restaurantDetails.getPreparationTimeMinutes());
        restaurant.setUpdatedAt(LocalDateTime.now());
        
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Successfully updated restaurant: {}", saved.getName());
        
        return saved;
    }
    
    /**
     * Deactivate a restaurant.
     */
    @Transactional
    public void deactivateRestaurant(UUID id) {
        log.info("Deactivating restaurant with ID: {}", id);
        
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + id));
        
        restaurant.setIsActive(false);
        restaurant.setDeactivatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());
        
        restaurantRepository.save(restaurant);
        log.info("Successfully deactivated restaurant: {}", restaurant.getName());
    }
    
    /**
     * Activate a restaurant.
     */
    @Transactional
    public void activateRestaurant(UUID id) {
        log.info("Activating restaurant with ID: {}", id);
        
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + id));
        
        restaurant.setIsActive(true);
        restaurant.setDeactivatedAt(null);
        restaurant.setUpdatedAt(LocalDateTime.now());
        
        restaurantRepository.save(restaurant);
        log.info("Successfully activated restaurant: {}", restaurant.getName());
    }
    
    /**
     * Get restaurant by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurant(UUID id) {
        return restaurantRepository.findById(id);
    }
    
    /**
     * Get restaurant by subdomain.
     */
    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantBySubdomain(String subdomain) {
        return restaurantRepository.findBySubdomain(subdomain);
    }
    
    /**
     * Get active restaurant by subdomain.
     */
    @Transactional(readOnly = true)
    public Optional<Restaurant> getActiveRestaurantBySubdomain(String subdomain) {
        return restaurantRepository.findBySubdomainAndIsActive(subdomain, true);
    }
    
    /**
     * Get all restaurants with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Restaurant> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable);
    }
    
    /**
     * Get active restaurants with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Restaurant> getActiveRestaurants(Pageable pageable) {
        return restaurantRepository.findActiveRestaurants(pageable);
    }
    
    /**
     * Get active restaurants that allow online orders.
     */
    @Transactional(readOnly = true)
    public Page<Restaurant> getActiveRestaurantsWithOnlineOrders(Pageable pageable) {
        return restaurantRepository.findActiveRestaurantsWithOnlineOrders(pageable);
    }
    
    /**
     * Search restaurants by name.
     */
    @Transactional(readOnly = true)
    public Page<Restaurant> searchRestaurantsByName(String name, Pageable pageable) {
        return restaurantRepository.findActiveRestaurantsByNameContaining(name, pageable);
    }
    
    /**
     * Get count of active restaurants.
     */
    @Transactional(readOnly = true)
    public long getActiveRestaurantCount() {
        return restaurantRepository.countActiveRestaurants();
    }
    
    /**
     * Delete a restaurant (soft delete).
     */
    @Transactional
    public void deleteRestaurant(UUID id) {
        log.info("Deleting restaurant with ID: {}", id);
        
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + id));
        
        restaurant.setIsDeleted(true);
        restaurant.setDeletedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());
        
        restaurantRepository.save(restaurant);
        log.info("Successfully deleted restaurant: {}", restaurant.getName());
    }
}
