package com.binarycliff.kitchenlab.tenant.controller;

import com.binarycliff.kitchenlab.tenant.entity.Restaurant;
import com.binarycliff.kitchenlab.tenant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Controller for system administration - managing restaurants and platform settings.
 * Only accessible by SUPER_ADMIN users.
 */
@Controller
@RequestMapping("/admin/system")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SystemAdminController {
    
    private final RestaurantService restaurantService;
    
    /**
     * Display system dashboard with restaurant overview.
     */
    @GetMapping("/dashboard")
    public String systemDashboard(Model model) {
        long activeRestaurants = restaurantService.getActiveRestaurantCount();
        
        model.addAttribute("activeRestaurants", activeRestaurants);
        model.addAttribute("pageTitle", "System Dashboard");
        
        return "admin/system/dashboard";
    }
    
    /**
     * Display list of all restaurants.
     */
    @GetMapping("/restaurants")
    public String listRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<Restaurant> restaurants = restaurantService.getAllRestaurants(pageable);
        
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", restaurants.getTotalPages());
        model.addAttribute("totalItems", restaurants.getTotalElements());
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("pageTitle", "Manage Restaurants");
        
        return "admin/system/restaurants";
    }
    
    /**
     * Display form to create new restaurant.
     */
    @GetMapping("/restaurants/new")
    public String createRestaurantForm(Model model) {
        model.addAttribute("restaurant", new Restaurant());
        model.addAttribute("pageTitle", "Create New Restaurant");
        model.addAttribute("isEdit", false);
        
        return "admin/system/restaurant-form";
    }
    
    /**
     * Process restaurant creation.
     */
    @PostMapping("/restaurants")
    public String createRestaurant(@Valid @ModelAttribute Restaurant restaurant,
                                   RedirectAttributes redirectAttributes) {
        try {
            Restaurant created = restaurantService.createRestaurant(restaurant);
            redirectAttributes.addFlashAttribute("success", 
                "Restaurant '" + created.getName() + "' created successfully!");
            return "redirect:/admin/system/restaurants";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/system/restaurants/new";
        }
    }
    
    /**
     * Display form to edit existing restaurant.
     */
    @GetMapping("/restaurants/{id}/edit")
    public String editRestaurantForm(@PathVariable UUID id, Model model) {
        Restaurant restaurant = restaurantService.getRestaurant(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + id));
        
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("pageTitle", "Edit Restaurant: " + restaurant.getName());
        model.addAttribute("isEdit", true);
        
        return "admin/system/restaurant-form";
    }
    
    /**
     * Process restaurant update.
     */
    @PostMapping("/restaurants/{id}")
    public String updateRestaurant(@PathVariable UUID id,
                                   @Valid @ModelAttribute Restaurant restaurant,
                                   RedirectAttributes redirectAttributes) {
        try {
            Restaurant updated = restaurantService.updateRestaurant(id, restaurant);
            redirectAttributes.addFlashAttribute("success", 
                "Restaurant '" + updated.getName() + "' updated successfully!");
            return "redirect:/admin/system/restaurants";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/system/restaurants/" + id + "/edit";
        }
    }
    
    /**
     * Deactivate a restaurant.
     */
    @PostMapping("/restaurants/{id}/deactivate")
    @ResponseBody
    public ResponseEntity<?> deactivateRestaurant(@PathVariable UUID id) {
        try {
            restaurantService.deactivateRestaurant(id);
            return ResponseEntity.ok().body("Restaurant deactivated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Activate a restaurant.
     */
    @PostMapping("/restaurants/{id}/activate")
    @ResponseBody
    public ResponseEntity<?> activateRestaurant(@PathVariable UUID id) {
        try {
            restaurantService.activateRestaurant(id);
            return ResponseEntity.ok().body("Restaurant activated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Delete a restaurant.
     */
    @PostMapping("/restaurants/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteRestaurant(@PathVariable UUID id) {
        try {
            restaurantService.deleteRestaurant(id);
            return ResponseEntity.ok().body("Restaurant deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * API endpoint to get restaurant details.
     */
    @GetMapping("/api/restaurants/{id}")
    @ResponseBody
    public ResponseEntity<Restaurant> getRestaurantApi(@PathVariable UUID id) {
        return restaurantService.getRestaurant(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * API endpoint to search restaurants.
     */
    @GetMapping("/api/restaurants/search")
    @ResponseBody
    public Page<Restaurant> searchRestaurants(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return restaurantService.searchRestaurantsByName(query, pageable);
    }
}
