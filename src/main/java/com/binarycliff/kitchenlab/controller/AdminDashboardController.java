package com.binarycliff.kitchenlab.controller;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.auth.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * MVC Controller for admin dashboard routing.
 * Routes users to appropriate dashboard based on their role.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {
    
    private final AdminRepository adminRepository;
    
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
    @GetMapping("/super-dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String superDashboard() {
        return "admin/Superadmin-admin-dashboard";
    }
    
    /**
     * RESTAURANT_ADMIN specific dashboard.
     */
    @GetMapping("/restaurant-dashboard")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public String restaurantDashboard() {
        return "admin/kitchenlab-admin";
    }
    
    /**
     * MANAGER specific dashboard.
     */
    @GetMapping("/manager-dashboard")
    @PreAuthorize("hasRole('MANAGER')")
    public String managerDashboard() {
        return "admin/manager-dashboard";
    }
    
    /**
     * STAFF specific dashboard.
     */
    @GetMapping("/staff-dashboard")
    @PreAuthorize("hasRole('STAFF')")
    public String staffDashboard() {
        return "admin/staff-dashboard";
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
