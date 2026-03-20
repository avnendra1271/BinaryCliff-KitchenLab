package com.binarycliff.kitchenlab.auth.filter;

import com.binarycliff.kitchenlab.tenant.context.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to set system context for login and authentication endpoints.
 * This allows super admin login from the main login page.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class LoginContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        
        // Set system context for login-related endpoints and admin dashboard
        if (requestURI.equals("/auth/login") || 
            requestURI.startsWith("/api/auth/") ||
            requestURI.equals("/login") ||
            requestURI.startsWith("/static/") ||
            requestURI.equals("/") ||
            requestURI.startsWith("/admin/")) {
            
            log.debug("Setting system context for request: {}", requestURI);
            TenantContext.setSystemContext();
        }
        
        try {
            chain.doFilter(request, response);
        } finally {
            // Always clear the context after the request
            TenantContext.clear();
        }
    }
}
