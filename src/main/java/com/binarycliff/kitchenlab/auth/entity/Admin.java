package com.binarycliff.kitchenlab.auth.entity;

import com.binarycliff.kitchenlab.common.entity.BaseEntity;
import com.binarycliff.kitchenlab.tenant.filter.TenantAware;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Admin user entity for authentication and authorization.
 */
@Entity
@Table(name = "admins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Admin extends BaseEntity implements UserDetails, TenantAware {
    
    @Column(unique = true, nullable = false, length = 100)
    private String username;
    
    @Column(nullable = false, length = 100)
    private String password;
    
    @Column(nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false, length = 100)
    private String firstName;
    
    @Column(nullable = false, length = 100)
    private String lastName;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 500)
    private String profileImageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole role;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;
    
    @Column
    private LocalDateTime lastLoginAt;
    
    @Column
    private String lastLoginIp;
    
    @Column
    private LocalDateTime passwordChangedAt;
    
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", insertable = false, updatable = false)
    private com.binarycliff.kitchenlab.tenant.entity.Restaurant restaurant;
    
    @ElementCollection
    @CollectionTable(name = "admin_permissions", joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "permission")
    private List<String> permissions;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }
    
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @Override
    public UUID getTenantId() {
        return restaurantId;
    }
    
    @Override
    public void setTenantId(UUID tenantId) {
        this.restaurantId = tenantId;
    }
    
    public enum AdminRole {
        SUPER_ADMIN("Super Admin", "Full system access (binaryCLiff owner)"),
        RESTAURANT_ADMIN("Restaurant Admin", "Full restaurant access (restaurant owner)"),
        MANAGER("Manager", "Order and menu management"),
        STAFF("Staff", "Order handling only");
        
        private final String displayName;
        private final String description;
        
        AdminRole(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
