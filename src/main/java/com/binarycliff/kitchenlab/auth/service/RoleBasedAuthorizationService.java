package com.binarycliff.kitchenlab.auth.service;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Role-based authorization service implementing production-ready role restrictions.
 * Enforces access control based on role definitions and responsibilities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleBasedAuthorizationService {

    /**
     * Check if user can access platform-level features (SUPER_ADMIN only).
     */
    public boolean canAccessPlatformFeatures(Admin user) {
        boolean hasAccess = user.getRole() == Admin.AdminRole.SUPER_ADMIN;
        log.debug("User {} can access platform features: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can manage restaurants (SUPER_ADMIN only).
     */
    public boolean canManageRestaurants(Admin user) {
        boolean hasAccess = user.getRole() == Admin.AdminRole.SUPER_ADMIN;
        log.debug("User {} can manage restaurants: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can manage users across platform (SUPER_ADMIN only).
     */
    public boolean canManagePlatformUsers(Admin user) {
        boolean hasAccess = user.getRole() == Admin.AdminRole.SUPER_ADMIN;
        log.debug("User {} can manage platform users: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can manage restaurant staff (RESTAURANT_ADMIN only).
     */
    public boolean canManageRestaurantStaff(Admin user, UUID restaurantId) {
        boolean hasAccess = user.getRole() == Admin.AdminRole.RESTAURANT_ADMIN && 
                           user.getRestaurantId().equals(restaurantId);
        log.debug("User {} can manage restaurant staff: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can access financial reports (RESTAURANT_ADMIN, SUPER_ADMIN).
     */
    public boolean canAccessFinancialReports(Admin user) {
        boolean hasAccess = user.getRole() == Admin.AdminRole.RESTAURANT_ADMIN || 
                           user.getRole() == Admin.AdminRole.SUPER_ADMIN;
        log.debug("User {} can access financial reports: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can manage menu (RESTAURANT_ADMIN, MANAGER).
     */
    public boolean canManageMenu(Admin user) {
        boolean hasAccess = user.getRole() == Admin.AdminRole.RESTAURANT_ADMIN || 
                           user.getRole() == Admin.AdminRole.MANAGER;
        log.debug("User {} can manage menu: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can view menu (All roles, but STAFF is view-only).
     */
    public boolean canViewMenu(Admin user) {
        boolean hasAccess = true; // All roles can view menu
        log.debug("User {} can view menu: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can manage orders (RESTAURANT_ADMIN, MANAGER, STAFF).
     */
    public boolean canManageOrders(Admin user) {
        boolean hasAccess = user.getRole() == Admin.AdminRole.RESTAURANT_ADMIN || 
                           user.getRole() == Admin.AdminRole.MANAGER ||
                           user.getRole() == Admin.AdminRole.STAFF;
        log.debug("User {} can manage orders: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can assign tasks to staff (MANAGER only).
     */
    public boolean canAssignTasks(Admin user) {
        boolean hasAccess = user.getRole() == Admin.AdminRole.MANAGER;
        log.debug("User {} can assign tasks: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can access inventory (MANAGER with limitations).
     */
    public boolean canAccessInventory(Admin user) {
        boolean hasAccess = user.getRole() == Admin.AdminRole.MANAGER;
        log.debug("User {} can access inventory: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can access reports (with role-based limitations).
     */
    public boolean canAccessReports(Admin user) {
        boolean hasAccess = user.getRole() == Admin.AdminRole.RESTAURANT_ADMIN || 
                           user.getRole() == Admin.AdminRole.MANAGER ||
                           user.getRole() == Admin.AdminRole.SUPER_ADMIN;
        log.debug("User {} can access reports: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can access system settings (SUPER_ADMIN only).
     */
    public boolean canAccessSystemSettings(Admin user) {
        boolean hasAccess = user.getRole() == Admin.AdminRole.SUPER_ADMIN;
        log.debug("User {} can access system settings: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can delete critical data (SUPER_ADMIN only).
     */
    public boolean canDeleteCriticalData(Admin user) {
        boolean hasAccess = user.getRole() == Admin.AdminRole.SUPER_ADMIN;
        log.debug("User {} can delete critical data: {}", user.getUsername(), hasAccess);
        return hasAccess;
    }

    /**
     * Check if user can access tenant data based on role and context.
     */
    public boolean canAccessTenantData(Admin user, UUID tenantId) {
        // SUPER_ADMIN can access all tenant data
        if (user.getRole() == Admin.AdminRole.SUPER_ADMIN) {
            return true;
        }
        
        // Other roles can only access their own tenant data
        boolean hasAccess = user.getRestaurantId().equals(tenantId);
        log.debug("User {} can access tenant {}: {}", user.getUsername(), tenantId, hasAccess);
        return hasAccess;
    }

    /**
     * Get user's access scope description.
     */
    public String getUserAccessScope(Admin user) {
        return switch (user.getRole()) {
            case SUPER_ADMIN -> "Global (All restaurants)";
            case RESTAURANT_ADMIN -> "Single restaurant (Restaurant Owner)";
            case MANAGER -> "Restaurant-level (Operations Head)";
            case STAFF -> "Operational only (Execution Role)";
        };
    }

    /**
     * Get user's responsibilities description.
     */
    public String getUserResponsibilities(Admin user) {
        return switch (user.getRole()) {
            case SUPER_ADMIN -> "Multi-tenant control, user management, platform configuration, global reports";
            case RESTAURANT_ADMIN -> "Restaurant management, staff management, menu control, financial reports";
            case MANAGER -> "Daily operations, order management, task assignment, limited reports";
            case STAFF -> "Order handling, task execution, limited menu access, status updates";
        };
    }
}
