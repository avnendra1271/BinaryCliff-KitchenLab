package com.binarycliff.kitchenlab.common.config;

import java.util.UUID;

/**
 * Context holder for restaurant-specific operations.
 * Provides thread-safe access to current restaurant ID.
 */
public class RestaurantContext {
    
    private static final ThreadLocal<UUID> currentRestaurantId = new ThreadLocal<>();
    
    /**
     * Set the current restaurant ID for the current thread.
     * 
     * @param restaurantId the restaurant ID to set
     */
    public static void setCurrentRestaurantId(UUID restaurantId) {
        currentRestaurantId.set(restaurantId);
    }
    
    /**
     * Get the current restaurant ID for the current thread.
     * 
     * @return the current restaurant ID
     * @throws IllegalStateException if no restaurant ID is set
     */
    public static UUID getCurrentRestaurantId() {
        UUID restaurantId = currentRestaurantId.get();
        if (restaurantId == null) {
            throw new IllegalStateException("No restaurant ID is set in the current context");
        }
        return restaurantId;
    }
    
    /**
     * Clear the current restaurant ID for the current thread.
     */
    public static void clear() {
        currentRestaurantId.remove();
    }
}
