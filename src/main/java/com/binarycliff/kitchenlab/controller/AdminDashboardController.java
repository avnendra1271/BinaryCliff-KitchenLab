package com.binarycliff.kitchenlab.controller;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.auth.repository.AdminRepository;
import com.binarycliff.kitchenlab.tenant.context.TenantContext;
import com.binarycliff.kitchenlab.tenant.entity.Restaurant;
import com.binarycliff.kitchenlab.tenant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

/**
 * MVC Controller for admin dashboard routing.
 * Routes users to appropriate dashboard based on their role.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {
    
    private final AdminRepository adminRepository;
    private final RestaurantRepository restaurantRepository;
    
    /**
     * Route to appropriate dashboard based on user role.
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            
            // Find admin user to get role
            Admin admin = adminRepository.findByUsername(userDetails.getUsername())
                    .orElse(null);
            
            if (admin == null) {
                return "redirect:/auth/login";
            }
            
            // Route based on role
            switch (admin.getRole()) {
                case SUPER_ADMIN:
                    return "redirect:/admin/super-dashboard";
                case RESTAURANT_ADMIN:
                    return "redirect:/admin/restaurant-dashboard";
                case MANAGER:
                    return "redirect:/admin/manager-dashboard";
                case STAFF:
                    return "redirect:/admin/staff-dashboard";
                default:
                    return "redirect:/auth/login";
            }
        }
        
        return "redirect:/auth/login";
    }
    
    /**
     * SUPER_ADMIN specific dashboard.
     */
    @GetMapping("/restaurant-dashboard")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public String restaurantDashboard(Model model) {
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Admin currentUser = adminRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found: " + auth.getName()));
            
            // Get current tenant context (set by TenantAuthenticationFilter)
            UUID currentTenant = TenantContext.getCurrentTenant();
            boolean isSystemContext = TenantContext.isSystemContext();
            
            log.info("Restaurant dashboard accessed by user: {}, tenant: {}, systemContext: {}", 
                    currentUser.getUsername(), currentTenant, isSystemContext);
            
            // Fetch restaurants (will be filtered by tenant context - should be only 1)
            List<Restaurant> restaurants = restaurantRepository.findAll();
            
            // Fetch admins for this restaurant only
            List<Admin> admins = adminRepository.findByRestaurantId(currentTenant);
            
            // Add data to model for restaurant management
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("restaurants", restaurants); // Will be 1 restaurant only
            model.addAttribute("admins", admins); // Will be restaurant's admins only
            model.addAttribute("currentTenant", currentTenant);
            model.addAttribute("isSystemContext", isSystemContext);
            model.addAttribute("tenantInfo", getTenantInfo(currentTenant, isSystemContext));
            
            // For RESTAURANT_ADMIN, use the kitchenlab-admin template for restaurant management
            return "admin/kitchenlab-admin";
            
        } catch (Exception e) {
            log.error("Error loading restaurant dashboard", e);
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "admin/kitchenlab-admin";
        }
    }
    
    /**
     * RESTAURANT_ADMIN specific dashboard.
     */

    @GetMapping("/super-dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String  superDashboard(Model model) {
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get current tenant context
            var currentTenant = TenantContext.getCurrentTenant();
            boolean isSystemContext = TenantContext.isSystemContext();
            
            log.info("Restaurant dashboard accessed by user: {}, tenant: {}, systemContext: {}", 
                    username, currentTenant, isSystemContext);
            
            // Get user details
            Admin currentUser = adminRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            // Get restaurants based on context
            List<Restaurant> restaurants;
            if (isSystemContext) {
                // Super admin can see all restaurants
                restaurants = restaurantRepository.findAll();
            } else {
                // Restaurant admin sees only their restaurant (filtered by tenant context)
                restaurants = restaurantRepository.findAll();
            }
            
            // Get admins based on context
            List<Admin> admins;
            if (isSystemContext) {
                // Super admin can see all admins
                admins = adminRepository.findAll();
            } else if (currentTenant != null) {
                // Restaurant admin sees only admins from their restaurant
                admins = adminRepository.findByRestaurantId(currentTenant);
            } else {
                admins = List.of();
            }
            
            // Add data to model
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("admins", admins);
            model.addAttribute("currentTenant", currentTenant);
            model.addAttribute("isSystemContext", isSystemContext);
            model.addAttribute("tenantInfo", getTenantInfo(currentTenant, isSystemContext));
            
            return "admin/tenant-dashboard";
            
        } catch (Exception e) {
            log.error("Error loading restaurant dashboard", e);
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "admin/tenant-dashboard";
        }
    }
    
    private String getTenantInfo(UUID currentTenant, boolean isSystemContext) {
        if (isSystemContext) {
            return "System Context - Can see all restaurants";
        } else if (currentTenant != null) {
            Restaurant restaurant = restaurantRepository.findById(currentTenant).orElse(null);
            if (restaurant != null) {
                return "Tenant: " + restaurant.getName() + " (" + restaurant.getSubdomain() + ")";
            }
        }
        return "No tenant context";
    }
    
    /**
     * MANAGER specific dashboard.
     */
    @GetMapping("/manager-dashboard")
    @PreAuthorize("hasRole('MANAGER')")
    public String managerDashboard(Model model) {
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Admin currentUser = adminRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found: " + auth.getName()));
            
            // Get current tenant context (set by TenantAuthenticationFilter)
            UUID currentTenant = TenantContext.getCurrentTenant();
            boolean isSystemContext = TenantContext.isSystemContext();
            
            log.info("Manager dashboard accessed by user: {}, tenant: {}, systemContext: {}", 
                    currentUser.getUsername(), currentTenant, isSystemContext);
            
            // Fetch restaurants (will be filtered by tenant context - should be 1)
            List<Restaurant> restaurants = restaurantRepository.findAll();
            
            // Fetch admins for this restaurant only
            List<Admin> admins = adminRepository.findByRestaurantId(currentTenant);
            
            // Add data to model
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("admins", admins);
            model.addAttribute("currentTenant", currentTenant);
            model.addAttribute("isSystemContext", isSystemContext);
            model.addAttribute("tenantInfo", getTenantInfo(currentTenant, isSystemContext));
            model.addAttribute("roleInfo", "Manager - Order and menu management");
            
            return "admin/manager-dashboard";
            
        } catch (Exception e) {
            log.error("Error loading manager dashboard", e);
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "admin/tenant-dashboard";
        }
    }
    
    /**
     * STAFF specific dashboard.
     */
    @GetMapping("/staff-dashboard")
    @PreAuthorize("hasRole('STAFF')")
    public String staffDashboard(Model model) {
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Admin currentUser = adminRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found: " + auth.getName()));
            
            // Get current tenant context (set by TenantAuthenticationFilter)
            UUID currentTenant = TenantContext.getCurrentTenant();
            boolean isSystemContext = TenantContext.isSystemContext();
            
            log.info("Staff dashboard accessed by user: {}, tenant: {}, systemContext: {}", 
                    currentUser.getUsername(), currentTenant, isSystemContext);
            
            // Fetch restaurants (will be filtered by tenant context - should be 1)
            List<Restaurant> restaurants = restaurantRepository.findAll();
            
            // Fetch admins for this restaurant only
            List<Admin> admins = adminRepository.findByRestaurantId(currentTenant);
            
            // Add data to model
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("admins", admins);
            model.addAttribute("currentTenant", currentTenant);
            model.addAttribute("isSystemContext", isSystemContext);
            model.addAttribute("tenantInfo", getTenantInfo(currentTenant, isSystemContext));
            model.addAttribute("roleInfo", "Staff - Order handling only");
            
            return "admin/staff-dashboard";
            
        } catch (Exception e) {
            log.error("Error loading staff dashboard", e);
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "admin/tenant-dashboard";
        }
    }
    
    /**
     * Redirect root admin to role-appropriate dashboard.
     */
    @GetMapping("/")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('RESTAURANT_ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public String adminRoot() {
        return dashboard();
    }
}
