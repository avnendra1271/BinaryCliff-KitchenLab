package com.binarycliff.kitchenlab.tenant.filter;

import java.util.UUID;

/**
 * Interface for entities that are tenant-aware.
 */
public interface TenantAware {
    
    /**
     * Get the tenant ID for this entity.
     */
    UUID getTenantId();
    
    /**
     * Set the tenant ID for this entity.
     */
    void setTenantId(UUID tenantId);
}
