package com.binarycliff.kitchenlab.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Tenant context management for multi-tenant architecture.
 * Provides thread-safe access to current restaurant/tenant context.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class TenantContext {

    private static final String TENANT_HEADER = "X-Restaurant-ID";
    private static final String TENANT_REQUEST_ATTR = "restaurantId";
    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * Set current tenant in thread context.
     * 
     * @param restaurantId the restaurant ID
     */
    public static void setCurrentTenant(UUID restaurantId) {
        log.debug("Setting current tenant: {}", restaurantId);
        CURRENT_TENANT.set(restaurantId);
    }

    /**
     * Get current tenant from thread context.
     * 
     * @return current restaurant ID or null if not set
     */
    public static UUID getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Get current tenant from request headers.
     * 
     * @return restaurant ID from request or null
     */
    public static UUID getCurrentTenantFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // Try request attribute first (set by filter)
                Object restaurantId = request.getAttribute(TENANT_REQUEST_ATTR);
                if (restaurantId instanceof UUID) {
                    return (UUID) restaurantId;
                }
                
                // Try header
                String tenantHeader = request.getHeader(TENANT_HEADER);
                if (tenantHeader != null && !tenantHeader.isEmpty()) {
                    try {
                        return UUID.fromString(tenantHeader);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid restaurant ID in header: {}", tenantHeader);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract tenant from request: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Clear current tenant from thread context.
     */
    public static void clearCurrentTenant() {
        log.debug("Clearing current tenant");
        CURRENT_TENANT.remove();
    }

    /**
     * Check if tenant context is available.
     * 
     * @return true if tenant is set, false otherwise
     */
    public static boolean hasCurrentTenant() {
        return getCurrentTenant() != null;
    }

    /**
     * Get current tenant as string for logging.
     * 
     * @return tenant ID as string or "unknown"
     */
    public static String getCurrentTenantId() {
        UUID tenant = getCurrentTenant();
        return tenant != null ? tenant.toString() : "unknown";
    }
}
