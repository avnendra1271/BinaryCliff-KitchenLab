package com.binarycliff.kitchenlab.tenant.interceptor;

import com.binarycliff.kitchenlab.tenant.context.TenantContext;
import com.binarycliff.kitchenlab.tenant.entity.Restaurant;
import com.binarycliff.kitchenlab.tenant.repository.RestaurantRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

/**
 * Interceptor to resolve tenant from request and set it in TenantContext.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {
    
    private final RestaurantRepository restaurantRepository;
    
    // Paths that don't require tenant resolution
    private static final List<String> SYSTEM_PATHS = Arrays.asList(
        "/auth", "/api/auth", "/h2-console", "/static", "/css", "/js", "/images",
        "/favicon.ico", "/error", "/actuator", "/admin/system"
    );
    
    // Subdomains that are reserved for system operations
    private static final List<String> SYSTEM_SUBDOMAINS = Arrays.asList(
        "www", "admin", "api", "app", "system", "root"
    );
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String serverName = request.getServerName();
        
        // Check if this is a system path
        boolean isSystemPath = SYSTEM_PATHS.stream().anyMatch(requestURI::startsWith);
        
        // Extract subdomain from server name
        String subdomain = extractSubdomain(serverName);
        
        // Handle system context
        if (isSystemPath || SYSTEM_SUBDOMAINS.contains(subdomain) || subdomain == null) {
            log.debug("Setting system context for path: {}, subdomain: {}", requestURI, subdomain);
            TenantContext.setSystemContext();
            return true;
        }
        
        // Resolve tenant from subdomain
        try {
            Restaurant restaurant = restaurantRepository.findBySubdomainAndIsActive(subdomain, true)
                .orElse(null);
            
            if (restaurant == null) {
                log.warn("Restaurant not found for subdomain: {}", subdomain);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Restaurant not found");
                return false;
            }
            
            log.debug("Setting tenant context for restaurant: {} (ID: {})", restaurant.getName(), restaurant.getId());
            TenantContext.setCurrentTenant(restaurant.getId(), subdomain);
            
            // Add tenant info to request attributes for templates
            request.setAttribute("currentRestaurant", restaurant);
            request.setAttribute("tenantSubdomain", subdomain);
            
        } catch (Exception e) {
            log.error("Error resolving tenant for subdomain: {}", subdomain, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Tenant resolution failed");
            return false;
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Clear tenant context after request completes
        TenantContext.clear();
    }
    
    /**
     * Extract subdomain from server name.
     * Examples:
     * - restaurant1.kitchenlab.com -> restaurant1
     * - kitchenlab.com -> null
     * - localhost -> null
     */
    private String extractSubdomain(String serverName) {
        if (serverName == null || serverName.isEmpty()) {
            return null;
        }
        
        // Handle localhost for development
        if (serverName.equals("localhost") || serverName.startsWith("127.0.0.1")) {
            return null;
        }
        
        String[] parts = serverName.split("\\.");
        if (parts.length < 3) {
            return null;
        }
        
        // Return the first part as subdomain
        return parts[0].toLowerCase();
    }
}
