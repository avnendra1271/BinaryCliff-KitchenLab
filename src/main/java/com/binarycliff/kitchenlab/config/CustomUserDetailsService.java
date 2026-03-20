package com.binarycliff.kitchenlab.config;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.auth.repository.AdminRepository;
import com.binarycliff.kitchenlab.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

/**
 * Custom UserDetailsService implementation with tenant awareness.
 * Loads user data from database for Spring Security.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user: {}", username);
        
        Admin admin;
        UUID currentTenant = TenantContext.getCurrentTenant();
        
        if (currentTenant != null) {
            // Tenant context available - search within tenant
            log.debug("Searching for user {} in tenant: {}", username, currentTenant);
            admin = adminRepository.findByUsernameAndRestaurantId(username, currentTenant)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        } else if (TenantContext.isSystemContext()) {
            // System context - allow super admin login
            log.debug("System context - searching for super admin user: {}", username);
            admin = adminRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            
            // Allow SUPER_ADMIN and RESTAURANT_ADMIN in system context
            if (admin.getRole() != Admin.AdminRole.SUPER_ADMIN && 
                admin.getRole() != Admin.AdminRole.RESTAURANT_ADMIN) {
                throw new UsernameNotFoundException("Access denied for user: " + username);
            }
        } else {
            throw new IllegalStateException("No tenant context available for authentication");
        }

        log.debug("Successfully loaded user: {} with role: {}", admin.getUsername(), admin.getRole());
        
        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!admin.getIsEnabled())
                .build();
    }
}
