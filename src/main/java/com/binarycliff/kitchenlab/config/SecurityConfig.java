package com.binarycliff.kitchenlab.config;

import com.binarycliff.kitchenlab.auth.filter.TenantAuthenticationFilter;
import com.binarycliff.kitchenlab.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Security configuration for the multi-tenant application.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantAuthenticationFilter tenantAuthenticationFilter;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - accessible without authentication
                .requestMatchers("/api/auth/**", "/h2-console/**", 
                               "/static/**", "/css/**", "/js/**", "/images/**", 
                               "/favicon.ico", "/error", "/actuator/**", "/api/setup/**", "/api/test/**", "/api/test/tenant/**").permitAll()
                
                // Auth endpoints - login page and related
                    .requestMatchers(
                            "/auth/login",
                            "/login",
                            "/auth/forgot-password",
                            "/auth/reset-password"
                    ).not().authenticated()
                
                // System admin endpoints - only SUPER_ADMIN
                .requestMatchers("/admin/system/**").hasRole("SUPER_ADMIN")
                
                // All admin endpoints require authentication
                .requestMatchers("/admin/**").authenticated()
                
                // API endpoints - require tenant context
                .requestMatchers("/api/**").access(this::hasTenantAccess)
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .addFilterBefore(tenantAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/auth/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
    
    /**
     * Custom authorization manager for tenant-based access control.
     */
    private AuthorizationDecision hasTenantAccess(Supplier<? extends Authentication> authentication, 
                                                  RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }
        
        // Super admins can access everything
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            // Set system context for super admins
            TenantContext.setSystemContext();
            return new AuthorizationDecision(true);
        }
        
        // Restaurant admins can access admin endpoints
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RESTAURANT_ADMIN"))) {
            // Set system context for restaurant admins for now
            // In production, you'd set their specific restaurant context
            TenantContext.setSystemContext();
            return new AuthorizationDecision(true);
        }
        
        // For other roles, require tenant context
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null && !TenantContext.isSystemContext()) {
            log.warn("Access denied: No tenant context for request: {}", context.getRequest().getRequestURI());
            return new AuthorizationDecision(false);
        }
        
        return new AuthorizationDecision(true);
    }
}
