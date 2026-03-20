package com.binarycliff.kitchenlab.tenant.context;

import java.util.UUID;

/**
 * Thread-local context holder for current tenant information.
 */
public class TenantContext {
    
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<String> currentSubdomain = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isSystemContext = new ThreadLocal<>();
    
    /**
     * Set the current tenant ID.
     */
    public static void setCurrentTenant(UUID tenantId) {
        currentTenant.set(tenantId);
        isSystemContext.set(false);
    }
    
    /**
     * Set the current tenant ID and subdomain.
     */
    public static void setCurrentTenant(UUID tenantId, String subdomain) {
        currentTenant.set(tenantId);
        currentSubdomain.set(subdomain);
        isSystemContext.set(false);
    }
    
    /**
     * Get the current tenant ID.
     */
    public static UUID getCurrentTenant() {
        return currentTenant.get();
    }
    
    /**
     * Get the current subdomain.
     */
    public static String getCurrentSubdomain() {
        return currentSubdomain.get();
    }
    
    /**
     * Check if current context is a system context (no specific tenant).
     */
    public static boolean isSystemContext() {
        return Boolean.TRUE.equals(isSystemContext.get());
    }
    
    /**
     * Set system context (no specific tenant).
     */
    public static void setSystemContext() {
        currentTenant.remove();
        currentSubdomain.remove();
        isSystemContext.set(true);
    }
    
    /**
     * Clear the current tenant context.
     */
    public static void clear() {
        currentTenant.remove();
        currentSubdomain.remove();
        isSystemContext.remove();
    }
    
    /**
     * Check if a tenant is currently set.
     */
    public static boolean hasTenant() {
        return currentTenant.get() != null;
    }
}
