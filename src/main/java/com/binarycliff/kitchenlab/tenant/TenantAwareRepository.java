package com.binarycliff.kitchenlab.tenant;

import com.binarycliff.kitchenlab.common.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Base repository interface for tenant-aware entities.
 * Automatically filters queries by current tenant context.
 * 
 * @param <T> Entity type
 * @param <ID> Entity ID type
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@NoRepositoryBean
public interface TenantAwareRepository<T extends BaseEntity, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * Find all entities for current tenant.
     * 
     * @return list of entities for current tenant
     */
    default List<T> findAllByCurrentTenant() {
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return findByRestaurantId(currentTenant);
    }

    /**
     * Find entity by ID for current tenant.
     * 
     * @param id entity ID
     * @return optional containing entity if found and belongs to current tenant
     */
    default Optional<T> findByIdAndCurrentTenant(ID id) {
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return findByIdAndRestaurantId(id, currentTenant);
    }

    /**
     * Save entity with current tenant context.
     * 
     * @param entity entity to save
     * @return saved entity
     */
    default <S extends T> S saveWithCurrentTenant(S entity) {
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            throw new IllegalStateException("No tenant context available");
        }
        
        // Set restaurant ID if entity supports it
        setRestaurantId(entity, currentTenant);
        
        return save(entity);
    }

    /**
     * Delete entity by ID for current tenant.
     * 
     * @param id entity ID
     */
    default void deleteByIdAndCurrentTenant(ID id) {
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            throw new IllegalStateException("No tenant context available");
        }
        
        Optional<T> entity = findByIdAndRestaurantId(id, currentTenant);
        entity.ifPresent(this::delete);
    }

    /**
     * Count entities for current tenant.
     * 
     * @return count of entities for current tenant
     */
    default long countByCurrentTenant() {
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return countByRestaurantId(currentTenant);
    }

    // Abstract methods to be implemented by specific repositories
    List<T> findByRestaurantId(UUID restaurantId);
    Optional<T> findByIdAndRestaurantId(ID id, UUID restaurantId);
    long countByRestaurantId(UUID restaurantId);
    
    /**
     * Helper method to set restaurant ID on entity using reflection.
     * 
     * @param entity the entity
     * @param restaurantId the restaurant ID
     */
    private void setRestaurantId(T entity, UUID restaurantId) {
        try {
            entity.getClass().getMethod("setRestaurantId", UUID.class).invoke(entity, restaurantId);
        } catch (Exception e) {
            // Entity doesn't have restaurantId field, which is fine
            // Some entities might not be tenant-specific
        }
    }
}
