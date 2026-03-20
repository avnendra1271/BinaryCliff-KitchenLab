package com.binarycliff.kitchenlab.controller;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.auth.repository.AdminRepository;
import com.binarycliff.kitchenlab.auth.service.RoleBasedAuthorizationService;
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
    private final RoleBasedAuthorizationService authorizationService;
    
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
     * RESTAURANT_ADMIN specific dashboard - Restaurant Owner with single restaurant scope.
     * Responsibilities: Restaurant management, staff management, menu control, financial reports.
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
            
            // Fetch restaurants based on context
            List<Restaurant> restaurants;
            if (isSystemContext) {
                // Super admin can see all restaurants
                restaurants = restaurantRepository.findAll();
            } else if (currentTenant != null) {
                // Restaurant users see only their restaurant
                Restaurant restaurant = restaurantRepository.findById(currentTenant).orElse(null);
                restaurants = restaurant != null ? List.of(restaurant) : List.of();
            } else {
                restaurants = List.of();
            }
            
            // Fetch admins for this restaurant only
            List<Admin> admins = adminRepository.findByRestaurantId(currentTenant);
            
            // Add data to model for restaurant management with role-based permissions
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("restaurants", restaurants); // Will be 1 restaurant only
            model.addAttribute("admins", admins); // Will be restaurant's admins only
            model.addAttribute("currentTenant", currentTenant);
            model.addAttribute("isSystemContext", isSystemContext);
            model.addAttribute("tenantInfo", getTenantInfo(currentTenant, isSystemContext));
            
            // Add role-specific permissions and information
            model.addAttribute("userAccessScope", authorizationService.getUserAccessScope(currentUser));
            model.addAttribute("userResponsibilities", authorizationService.getUserResponsibilities(currentUser));
            model.addAttribute("canManageRestaurantStaff", authorizationService.canManageRestaurantStaff(currentUser, currentTenant));
            model.addAttribute("canAccessFinancialReports", authorizationService.canAccessFinancialReports(currentUser));
            model.addAttribute("canManageMenu", authorizationService.canManageMenu(currentUser));
            model.addAttribute("canManageOrders", authorizationService.canManageOrders(currentUser));
            model.addAttribute("canViewMenu", authorizationService.canViewMenu(currentUser));
            model.addAttribute("roleInfo", "Restaurant Owner - Single restaurant management");
            
            // For RESTAURANT_ADMIN, use the kitchenlab-admin template for restaurant management
            return "admin/kitchenlab-admin";
            
        } catch (Exception e) {
            log.error("Error loading restaurant dashboard", e);
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "admin/kitchenlab-admin";
        }
    }
    
    /**
     * SUPER_ADMIN specific dashboard - Platform Owner with global scope.
     * Responsibilities: Multi-tenant control, user management, platform configuration.
     */

    @GetMapping("/super-dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String superDashboard(Model model) {
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
            
            // Add data to model with role-based permissions
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("admins", admins);
            model.addAttribute("currentTenant", currentTenant);
            model.addAttribute("isSystemContext", isSystemContext);
            model.addAttribute("tenantInfo", getTenantInfo(currentTenant, isSystemContext));
            
            // Add role-specific permissions and information
            model.addAttribute("userAccessScope", authorizationService.getUserAccessScope(currentUser));
            model.addAttribute("userResponsibilities", authorizationService.getUserResponsibilities(currentUser));
            model.addAttribute("canManageRestaurants", authorizationService.canManageRestaurants(currentUser));
            model.addAttribute("canManagePlatformUsers", authorizationService.canManagePlatformUsers(currentUser));
            model.addAttribute("canAccessFinancialReports", authorizationService.canAccessFinancialReports(currentUser));
            model.addAttribute("canAccessSystemSettings", authorizationService.canAccessSystemSettings(currentUser));
            model.addAttribute("canDeleteCriticalData", authorizationService.canDeleteCriticalData(currentUser));
            model.addAttribute("roleInfo", "Platform Owner - Multi-tenant control");
            
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
     * MANAGER specific dashboard - Operations Head with restaurant-level limited control.
     * Responsibilities: Daily operations, order management, task assignment, limited reports.
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
            
            // Fetch restaurants based on context
            List<Restaurant> restaurants;
            if (isSystemContext) {
                // Super admin can see all restaurants
                restaurants = restaurantRepository.findAll();
            } else if (currentTenant != null) {
                // Restaurant users see only their restaurant
                Restaurant restaurant = restaurantRepository.findById(currentTenant).orElse(null);
                restaurants = restaurant != null ? List.of(restaurant) : List.of();
            } else {
                restaurants = List.of();
            }
            
            // Fetch admins for this restaurant only
            List<Admin> admins = adminRepository.findByRestaurantId(currentTenant);
            
            // Add data to model with role-based permissions
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("admins", admins);
            model.addAttribute("currentTenant", currentTenant);
            model.addAttribute("isSystemContext", isSystemContext);
            model.addAttribute("tenantInfo", getTenantInfo(currentTenant, isSystemContext));
            
            // Add role-specific permissions and information
            model.addAttribute("userAccessScope", authorizationService.getUserAccessScope(currentUser));
            model.addAttribute("userResponsibilities", authorizationService.getUserResponsibilities(currentUser));
            model.addAttribute("canManageOrders", authorizationService.canManageOrders(currentUser));
            model.addAttribute("canManageMenu", authorizationService.canManageMenu(currentUser));
            model.addAttribute("canViewMenu", authorizationService.canViewMenu(currentUser));
            model.addAttribute("canAssignTasks", authorizationService.canAssignTasks(currentUser));
            model.addAttribute("canAccessInventory", authorizationService.canAccessInventory(currentUser));
            model.addAttribute("canAccessReports", authorizationService.canAccessReports(currentUser));
            model.addAttribute("canAccessFinancialReports", authorizationService.canAccessFinancialReports(currentUser)); // Will be false for MANAGER
            model.addAttribute("roleInfo", "Operations Head - Restaurant-level limited control");
            
            return "admin/manager-dashboard";
            
        } catch (Exception e) {
            log.error("Error loading manager dashboard", e);
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "admin/tenant-dashboard";
        }
    }
    
    /**
     * STAFF specific dashboard - Execution Role with operational only scope.
     * Responsibilities: Order handling, task execution, limited menu access, status updates.
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
            
            // Fetch restaurants based on context
            List<Restaurant> restaurants;
            if (isSystemContext) {
                // Super admin can see all restaurants
                restaurants = restaurantRepository.findAll();
            } else if (currentTenant != null) {
                // Restaurant users see only their restaurant
                Restaurant restaurant = restaurantRepository.findById(currentTenant).orElse(null);
                restaurants = restaurant != null ? List.of(restaurant) : List.of();
            } else {
                restaurants = List.of();
            }
            
            // Fetch admins for this restaurant only
            List<Admin> admins = adminRepository.findByRestaurantId(currentTenant);
            
            // Add data to model with role-based permissions
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("admins", admins);
            model.addAttribute("currentTenant", currentTenant);
            model.addAttribute("isSystemContext", isSystemContext);
            model.addAttribute("tenantInfo", getTenantInfo(currentTenant, isSystemContext));
            
            // Add role-specific permissions and information
            model.addAttribute("userAccessScope", authorizationService.getUserAccessScope(currentUser));
            model.addAttribute("userResponsibilities", authorizationService.getUserResponsibilities(currentUser));
            model.addAttribute("canManageOrders", authorizationService.canManageOrders(currentUser));
            model.addAttribute("canViewMenu", authorizationService.canViewMenu(currentUser));
            model.addAttribute("canAccessReports", authorizationService.canAccessReports(currentUser)); // Will be false for STAFF
            model.addAttribute("canAccessFinancialReports", authorizationService.canAccessFinancialReports(currentUser)); // Will be false for STAFF
            model.addAttribute("canManageMenu", authorizationService.canManageMenu(currentUser)); // Will be false for STAFF
            model.addAttribute("canAssignTasks", authorizationService.canAssignTasks(currentUser)); // Will be false for STAFF
            model.addAttribute("canAccessInventory", authorizationService.canAccessInventory(currentUser)); // Will be false for STAFF
            model.addAttribute("roleInfo", "Execution Role - Operational only");
            
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
