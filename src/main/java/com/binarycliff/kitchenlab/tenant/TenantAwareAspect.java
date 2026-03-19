package com.binarycliff.kitchenlab.tenant;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Aspect for tenant-aware operations in multi-tenant architecture.
 * Automatically validates tenant context and injects tenant ID into method parameters.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Aspect
@Component
public class TenantAwareAspect {

    /**
     * Intercept methods that have restaurantId parameter and ensure tenant context is set.
     * This provides automatic tenant validation for service layer methods.
     */
    @Before("execution(* com.binarycliff.kitchenlab..*(.., java.util.UUID restaurantId, ..))")
    public void validateTenantContext(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        
        // Find restaurantId parameter
        UUID restaurantId = null;
        for (Object arg : args) {
            if (arg instanceof UUID) {
                restaurantId = (UUID) arg;
                break;
            }
        }
        
        if (restaurantId != null) {
            // Validate that the restaurantId matches current tenant context
            UUID currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant != null && !currentTenant.equals(restaurantId)) {
                log.warn("Tenant mismatch detected. Method: {}, Expected: {}, Actual: {}", 
                        joinPoint.getSignature().getName(), currentTenant, restaurantId);
                
                throw new SecurityException(
                        String.format("Tenant validation failed. Expected: %s, Provided: %s", 
                                currentTenant, restaurantId));
            }
            
            log.debug("Tenant validation passed for method: {}, restaurant: {}", 
                    joinPoint.getSignature().getName(), restaurantId);
        }
    }

    /**
     * Before advice for controller methods to set tenant context from request parameters.
     */
    @Before("@within(org.springframework.web.bind.annotation.RestController)")
    public void setTenantFromController(JoinPoint joinPoint) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // Extract restaurantId from request parameters
                String restaurantIdParam = request.getParameter("restaurantId");
                if (restaurantIdParam != null && !restaurantIdParam.isEmpty()) {
                    UUID restaurantId = UUID.fromString(restaurantIdParam);
                    TenantContext.setCurrentTenant(restaurantId);
                    
                    log.debug("Tenant context set from controller parameter: {} for method: {}", 
                            restaurantId, joinPoint.getSignature().getName());
                }
            }
        } catch (Exception e) {
            log.debug("Could not set tenant context in controller advice: {}", e.getMessage());
        }
    }
}
