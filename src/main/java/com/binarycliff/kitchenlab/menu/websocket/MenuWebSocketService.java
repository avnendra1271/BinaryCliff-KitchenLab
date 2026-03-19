package com.binarycliff.kitchenlab.menu.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * WebSocket service for real-time menu updates.
 * Handles broadcasting menu changes to connected clients.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket destinations
    private static final String MENU_UPDATES_DESTINATION = "/topic/menu-updates";
    private static final String RESTAURANT_MENU_DESTINATION = "/topic/restaurant/{restaurantId}/menu";
    private static final String CATEGORY_UPDATES_DESTINATION = "/topic/restaurant/{restaurantId}/categories";
    private static final String ITEM_UPDATES_DESTINATION = "/topic/restaurant/{restaurantId}/items";

    /**
     * Broadcast menu category creation to all clients.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryData the created category data
     */
    @Async
    public void broadcastCategoryCreated(UUID restaurantId, Map<String, Object> categoryData) {
        log.info("Broadcasting category creation for restaurant: {}", restaurantId);
        
        Map<String, Object> message = Map.of(
                "type", "CATEGORY_CREATED",
                "restaurantId", restaurantId.toString(),
                "data", categoryData,
                "timestamp", LocalDateTime.now().toString()
        );
        
        // Send to general menu updates
        messagingTemplate.convertAndSend((Object) MENU_UPDATES_DESTINATION, message);
        
        // Send to restaurant-specific menu updates
        String restaurantDestination = RESTAURANT_MENU_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) restaurantDestination, message);
        
        // Send to category-specific updates
        String categoryDestination = CATEGORY_UPDATES_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) categoryDestination, message);
    }

    /**
     * Broadcast menu category update to all clients.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryData the updated category data
     */
    @Async
    public void broadcastCategoryUpdated(UUID restaurantId, Map<String, Object> categoryData) {
        log.info("Broadcasting category update for restaurant: {}", restaurantId);
        
        Map<String, Object> message = Map.of(
                "type", "CATEGORY_UPDATED",
                "restaurantId", restaurantId.toString(),
                "data", categoryData,
                "timestamp", LocalDateTime.now().toString()
        );
        
        // Send to general menu updates
        messagingTemplate.convertAndSend((Object) MENU_UPDATES_DESTINATION, message);
        
        // Send to restaurant-specific menu updates
        String restaurantDestination = RESTAURANT_MENU_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) restaurantDestination, message);
        
        // Send to category-specific updates
        String categoryDestination = CATEGORY_UPDATES_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) categoryDestination, message);
    }

    /**
     * Broadcast menu category deletion to all clients.
     * 
     * @param restaurantId the restaurant ID
     * @param categoryId the deleted category ID
     */
    @Async
    public void broadcastCategoryDeleted(UUID restaurantId, UUID categoryId) {
        log.info("Broadcasting category deletion for restaurant: {}", restaurantId);
        
        Map<String, Object> message = Map.of(
                "type", "CATEGORY_DELETED",
                "restaurantId", restaurantId.toString(),
                "categoryId", categoryId.toString(),
                "timestamp", LocalDateTime.now().toString()
        );
        
        // Send to general menu updates
        messagingTemplate.convertAndSend((Object) MENU_UPDATES_DESTINATION, message);
        
        // Send to restaurant-specific menu updates
        String restaurantDestination = RESTAURANT_MENU_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) restaurantDestination, message);
        
        // Send to category-specific updates
        String categoryDestination = CATEGORY_UPDATES_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) categoryDestination, message);
    }

    /**
     * Broadcast menu item creation to all clients.
     * 
     * @param restaurantId the restaurant ID
     * @param itemData the created menu item data
     */
    @Async
    public void broadcastMenuItemCreated(UUID restaurantId, Map<String, Object> itemData) {
        log.info("Broadcasting menu item creation for restaurant: {}", restaurantId);
        
        Map<String, Object> message = Map.of(
                "type", "MENU_ITEM_CREATED",
                "restaurantId", restaurantId.toString(),
                "data", itemData,
                "timestamp", LocalDateTime.now().toString()
        );
        
        // Send to general menu updates
        messagingTemplate.convertAndSend((Object) MENU_UPDATES_DESTINATION, message);
        
        // Send to restaurant-specific menu updates
        String restaurantDestination = RESTAURANT_MENU_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) restaurantDestination, message);
        
        // Send to item-specific updates
        String itemDestination = ITEM_UPDATES_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) itemDestination, message);
    }

    /**
     * Broadcast menu item update to all clients.
     * 
     * @param restaurantId the restaurant ID
     * @param itemData the updated menu item data
     */
    @Async
    public void broadcastMenuItemUpdated(UUID restaurantId, Map<String, Object> itemData) {
        log.info("Broadcasting menu item update for restaurant: {}", restaurantId);
        
        Map<String, Object> message = Map.of(
                "type", "MENU_ITEM_UPDATED",
                "restaurantId", restaurantId.toString(),
                "data", itemData,
                "timestamp", LocalDateTime.now().toString()
        );
        
        // Send to general menu updates
        messagingTemplate.convertAndSend((Object) MENU_UPDATES_DESTINATION, message);
        
        // Send to restaurant-specific menu updates
        String restaurantDestination = RESTAURANT_MENU_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) restaurantDestination, message);
        
        // Send to item-specific updates
        String itemDestination = ITEM_UPDATES_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) itemDestination, message);
    }

    /**
     * Broadcast menu item deletion to all clients.
     * 
     * @param restaurantId the restaurant ID
     * @param itemId the deleted menu item ID
     */
    @Async
    public void broadcastMenuItemDeleted(UUID restaurantId, UUID itemId) {
        log.info("Broadcasting menu item deletion for restaurant: {}", restaurantId);
        
        Map<String, Object> message = Map.of(
                "type", "MENU_ITEM_DELETED",
                "restaurantId", restaurantId.toString(),
                "itemId", itemId.toString(),
                "timestamp", LocalDateTime.now().toString()
        );
        
        // Send to general menu updates
        messagingTemplate.convertAndSend((Object) MENU_UPDATES_DESTINATION, message);
        
        // Send to restaurant-specific menu updates
        String restaurantDestination = RESTAURANT_MENU_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) restaurantDestination, message);
        
        // Send to item-specific updates
        String itemDestination = ITEM_UPDATES_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) itemDestination, message);
    }

    /**
     * Broadcast menu item availability change.
     * 
     * @param restaurantId the restaurant ID
     * @param itemData the menu item data with updated availability
     */
    @Async
    public void broadcastMenuItemAvailabilityChanged(UUID restaurantId, Map<String, Object> itemData) {
        log.info("Broadcasting menu item availability change for restaurant: {}", restaurantId);
        
        Map<String, Object> message = Map.of(
                "type", "MENU_ITEM_AVAILABILITY_CHANGED",
                "restaurantId", restaurantId.toString(),
                "data", itemData,
                "timestamp", LocalDateTime.now().toString()
        );
        
        // Send to general menu updates
        messagingTemplate.convertAndSend((Object) MENU_UPDATES_DESTINATION, message);
        
        // Send to restaurant-specific menu updates
        String restaurantDestination = RESTAURANT_MENU_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) restaurantDestination, message);
        
        // Send to item-specific updates
        String itemDestination = ITEM_UPDATES_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) itemDestination, message);
    }

    /**
     * Broadcast complete menu reload signal.
     * Useful when major changes require full menu refresh.
     * 
     * @param restaurantId the restaurant ID
     */
    @Async
    public void broadcastMenuReload(UUID restaurantId) {
        log.info("Broadcasting menu reload signal for restaurant: {}", restaurantId);
        
        Map<String, Object> message = Map.of(
                "type", "MENU_RELOAD",
                "restaurantId", restaurantId.toString(),
                "timestamp", LocalDateTime.now().toString()
        );
        
        // Send to restaurant-specific menu updates
        String restaurantDestination = RESTAURANT_MENU_DESTINATION.replace("{restaurantId}", restaurantId.toString());
        messagingTemplate.convertAndSend((Object) restaurantDestination, message);
    }

    /**
     * Send menu statistics update to admin clients.
     * 
     * @param restaurantId the restaurant ID
     * @param statistics the updated statistics
     */
    @Async
    public void broadcastMenuStatistics(UUID restaurantId, Map<String, Object> statistics) {
        log.info("Broadcasting menu statistics for restaurant: {}", restaurantId);
        
        Map<String, Object> message = Map.of(
                "type", "MENU_STATISTICS",
                "restaurantId", restaurantId.toString(),
                "data", statistics,
                "timestamp", LocalDateTime.now().toString()
        );
        
        // Send to restaurant-specific admin updates
        String adminDestination = "/topic/restaurant/" + restaurantId + "/admin/menu";
        messagingTemplate.convertAndSend((Object) adminDestination, message);
    }
}
