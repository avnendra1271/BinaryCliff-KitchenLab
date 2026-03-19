package com.binarycliff.kitchenlab.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to extract and set tenant context for multi-tenant architecture.
 * Automatically identifies the restaurant/tenant from request and sets it in thread context.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Restaurant-ID";
    private static final String TENANT_PARAM = "restaurantId";
    private static final String SUBDOMAIN_PATTERN = "^([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})\\.";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            UUID restaurantId = extractTenantId(httpRequest);
            
            // Skip tenant filtering for authentication endpoints
            String requestURI = httpRequest.getRequestURI();
            if (requestURI != null && (requestURI.contains("/api/auth/") || requestURI.contains("/auth/login"))) {
                log.debug("Skipping tenant filter for authentication endpoint: {}", requestURI);
                chain.doFilter(request, response);
                return;
            }
            
            if (restaurantId != null) {
                // Set in thread context
                TenantContext.setCurrentTenant(restaurantId);
                
                // Set as request attribute for access
                httpRequest.setAttribute("restaurantId", restaurantId);
                
                log.debug("Tenant context set: {} for request: {}", 
                        restaurantId, httpRequest.getRequestURI());
            } else {
                log.warn("No tenant identifier found in request: {}", httpRequest.getRequestURI());
                
                // For development, you might want to set a default tenant
                // TenantContext.setCurrentTenant(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            }
            
            chain.doFilter(request, response);
            
        } finally {
            // Always clear tenant context after request
            TenantContext.clearCurrentTenant();
        }
    }

    /**
     * Extract tenant ID from various sources in order of priority:
     * 1. Request parameter (restaurantId)
     * 2. HTTP header (X-Restaurant-ID)
     * 3. Subdomain pattern
     * 
     * @param request the HTTP request
     * @return restaurant ID or null if not found
     */
    private UUID extractTenantId(HttpServletRequest request) {
        // 1. Try request parameter first (highest priority for API calls)
        String restaurantIdParam = request.getParameter(TENANT_PARAM);
        if (restaurantIdParam != null && !restaurantIdParam.isEmpty()) {
            try {
                return UUID.fromString(restaurantIdParam);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid restaurant ID in parameter: {}", restaurantIdParam);
            }
        }
        
        // 2. Try HTTP header
        String tenantHeader = request.getHeader(TENANT_HEADER);
        if (tenantHeader != null && !tenantHeader.isEmpty()) {
            try {
                return UUID.fromString(tenantHeader);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid restaurant ID in header: {}", tenantHeader);
            }
        }
        
        // 3. Try subdomain pattern (for web interface)
        String host = request.getServerName();
        if (host != null && host.matches(SUBDOMAIN_PATTERN)) {
            try {
                String uuid = host.split("\\.")[0];
                return UUID.fromString(uuid);
            } catch (Exception e) {
                log.debug("Could not extract tenant from subdomain: {}", host);
            }
        }
        
        return null;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("TenantFilter initialized");
    }

    @Override
    public void destroy() {
        log.info("TenantFilter destroyed");
    }
}
