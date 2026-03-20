package com.binarycliff.kitchenlab.auth.filter;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.auth.repository.AdminRepository;
import com.binarycliff.kitchenlab.tenant.context.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to set tenant context based on authenticated user's restaurant.
 * This ensures that when a restaurant admin logs in, they only see their own data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantAuthenticationFilter implements Filter {

    private final AdminRepository adminRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Check if user is authenticated
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.isAuthenticated() && 
                auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                
                String username = userDetails.getUsername();
                log.debug("Setting tenant context for authenticated user: {}", username);
                
                // Find the admin user
                Admin admin = adminRepository.findByUsername(username)
                        .orElse(null);
                
                if (admin != null) {
                    // Set tenant context based on user's restaurant
                    TenantContext.setCurrentTenant(admin.getRestaurantId());
                    
                    // For restaurant admins, we could also set subdomain if needed
                    // This would require fetching the restaurant details
                    log.debug("Set tenant context for user {} with restaurantId: {}", 
                            username, admin.getRestaurantId());
                }
            }
            
            chain.doFilter(request, response);
            
        } finally {
            // Always clear tenant context after request
            TenantContext.clear();
        }
    }
}
