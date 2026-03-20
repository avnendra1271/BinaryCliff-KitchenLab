package com.binarycliff.kitchenlab.auth.filter;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.tenant.context.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to set tenant context after successful authentication.
 * This ensures that logged-in users have proper tenant context for accessing admin endpoints.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
public class TenantContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        
        // Set tenant context for authenticated users accessing admin endpoints
        if (requestURI.startsWith("/admin/") && !requestURI.equals("/admin/system/")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.isAuthenticated()) {
                // Check if user is Admin and set tenant context
                if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                    log.debug("Setting tenant context for authenticated user: {}", userDetails.getUsername());
                    
                    // For now, we'll use the demo restaurant as default tenant
                    // In a real multi-tenant scenario, this would be determined by subdomain or user selection
                    try {
                        // This is a simplified approach - in production, you'd determine the restaurant ID
                        // based on the user's role, subdomain, or user selection
                        if (auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_RESTAURANT_ADMIN"))) {
                            // For restaurant admin, set their restaurant as tenant
                            // In a real implementation, you'd fetch the user's restaurant ID from database
                            log.debug("Setting tenant context for restaurant admin");
                            // For demo purposes, we'll set system context for restaurant admins too
                            TenantContext.setSystemContext();
                        }
                    } catch (Exception e) {
                        log.error("Error setting tenant context", e);
                    }
                }
            }
        }
        
        try {
            chain.doFilter(request, response);
        } finally {
            // Don't clear context here as it might be needed by other filters
        }
    }
}
