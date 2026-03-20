package com.binarycliff.kitchenlab.tenant.filter;

import com.binarycliff.kitchenlab.tenant.context.TenantContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * JPA entity listener to automatically filter queries by tenant.
 */
@Component
public class TenantFilter {
    
    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof TenantAware) {
            TenantAware tenantAware = (TenantAware) entity;
            UUID currentTenant = TenantContext.getCurrentTenant();
            
            if (currentTenant != null) {
                tenantAware.setTenantId(currentTenant);
            } else if (!TenantContext.isSystemContext()) {
                throw new IllegalStateException("No tenant context available for entity creation");
            }
        }
    }
    
    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof TenantAware) {
            TenantAware tenantAware = (TenantAware) entity;
            UUID currentTenant = TenantContext.getCurrentTenant();
            
            if (currentTenant != null && !currentTenant.equals(tenantAware.getTenantId())) {
                throw new IllegalStateException("Cannot update entity from different tenant");
            }
        }
    }
    
    @PreRemove
    public void preRemove(Object entity) {
        if (entity instanceof TenantAware) {
            TenantAware tenantAware = (TenantAware) entity;
            UUID currentTenant = TenantContext.getCurrentTenant();
            
            if (currentTenant != null && !currentTenant.equals(tenantAware.getTenantId())) {
                throw new IllegalStateException("Cannot delete entity from different tenant");
            }
        }
    }
}
