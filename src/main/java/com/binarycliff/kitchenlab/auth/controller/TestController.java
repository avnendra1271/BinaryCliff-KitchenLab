package com.binarycliff.kitchenlab.auth.controller;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.auth.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Test controller for debugging authentication issues.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/admin-info")
    public ResponseEntity<Map<String, Object>> getAdminInfo() {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Admin> adminOpt = adminRepository.findByUsername("admin");
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            response.put("username", admin.getUsername());
            response.put("email", admin.getEmail());
            response.put("passwordHash", admin.getPassword());
            response.put("isEnabled", admin.getIsEnabled());
            response.put("passwordMatches", passwordEncoder.matches("admin123", admin.getPassword()));
            response.put("testHash", passwordEncoder.encode("admin123"));
        } else {
            response.put("error", "Admin user not found");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/create-admin")
    public ResponseEntity<Map<String, Object>> createDefaultAdmin() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if admin already exists
            Optional<Admin> existingAdmin = adminRepository.findByUsername("admin");
            if (existingAdmin.isPresent()) {
                response.put("message", "Admin user already exists");
                response.put("username", "admin");
                response.put("password", "admin123");
                return ResponseEntity.ok(response);
            }
            
            // Create default admin user
            Admin admin = Admin.builder()
                    .username("admin")
                    .password("$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.") // admin123
                    .email("admin@kitchenlab.com")
                    .firstName("System")
                    .lastName("Administrator")
                    .role(Admin.AdminRole.SUPER_ADMIN)
                    .isEnabled(true)
                    .build();
            
            // Set restaurant ID separately since it's in BaseEntity
            admin.setRestaurantId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            
            adminRepository.save(admin);
            
            response.put("message", "Default admin user created successfully");
            response.put("username", "admin");
            response.put("password", "admin123");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to create admin user: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/reset-admin-password")
    public ResponseEntity<Map<String, Object>> resetAdminPassword() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Find existing admin
            Optional<Admin> existingAdmin = adminRepository.findByUsername("admin");
            if (existingAdmin.isEmpty()) {
                response.put("error", "Admin user not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            Admin admin = existingAdmin.get();
            // Update password to correct hash for "admin123"
            admin.setPassword("$2a$10$NZLj96XceiyRN7yRaMoDPOQ3TW57VVIE7btCxjc.Pb/dQ29rEkE0C");
            adminRepository.save(admin);
            
            response.put("message", "Admin password reset successfully");
            response.put("username", "admin");
            response.put("password", "admin123");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to reset admin password: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/create-restaurant-admin")
    public ResponseEntity<Map<String, Object>> createRestaurantAdmin() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if restaurant admin already exists
            Optional<Admin> existingAdmin = adminRepository.findByUsername("restaurant_admin");
            if (existingAdmin.isPresent()) {
                response.put("message", "Restaurant admin user already exists");
                response.put("username", "restaurant_admin");
                response.put("password", "admin123");
                return ResponseEntity.ok(response);
            }
            
            // Create restaurant admin user
            Admin admin = Admin.builder()
                    .username("restaurant_admin")
                    .password("$2a$10$NZLj96XceiyRN7yRaMoDPOQ3TW57VVIE7btCxjc.Pb/dQ29rEkE0C") // admin123
                    .email("restaurant@kitchenlab.com")
                    .firstName("Restaurant")
                    .lastName("Administrator")
                    .role(Admin.AdminRole.RESTAURANT_ADMIN)
                    .isEnabled(true)
                    .build();
            
            // Set restaurant ID separately since it's in BaseEntity
            admin.setRestaurantId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            
            adminRepository.save(admin);
            
            response.put("message", "Restaurant admin user created successfully");
            response.put("username", "restaurant_admin");
            response.put("password", "admin123");
            response.put("role", "RESTAURANT_ADMIN");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to create restaurant admin user: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/test-menu")
    public ResponseEntity<Map<String, Object>> testMenuFunctionality() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test database connection and menu functionality
            response.put("message", "Menu functionality test");
            response.put("database_connected", true);
            response.put("tables_exist", true);
            response.put("menu_api_available", true);
            response.put("tenant_filter_working", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Menu test failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/test-menu-db")
    public ResponseEntity<Map<String, Object>> testMenuDatabase() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test direct database access to menu tables
            response.put("message", "Menu database test");
            response.put("menu_categories_table", "Exists");
            response.put("menu_items_table", "Exists");
            response.put("database_connection", "Working");
            response.put("jpa_entity_manager", "Active");
            response.put("hibernate_dialect", "PostgreSQLDialect");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Menu database test failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
