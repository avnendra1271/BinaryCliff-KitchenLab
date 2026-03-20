package com.binarycliff.kitchenlab.config;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.auth.repository.AdminRepository;
import com.binarycliff.kitchenlab.tenant.entity.Restaurant;
import com.binarycliff.kitchenlab.tenant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Data initialization component to create default data for the application.
 * Creates default restaurant and admin user when the application starts.
 * Ordered to run after JPA has initialized the database schema.
 */
//@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final AdminRepository adminRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        log.info("Initializing application data after application startup...");
        
        try {
            // Create default restaurant first
            Restaurant defaultRestaurant = createDefaultRestaurant();
            
            // Create default admin user
            createDefaultAdminUser(defaultRestaurant.getId());
            
            log.info("Application data initialization completed successfully");
        } catch (Exception e) {
            log.error("Error during data initialization: {}", e.getMessage(), e);
            // Don't fail the application if data initialization fails
        }
    }

    private Restaurant createDefaultRestaurant() {
        try {
            return restaurantRepository.findBySubdomain("demo")
                    .orElseGet(() -> {
                        Restaurant restaurant = new Restaurant();
                        restaurant.setName("Demo Restaurant");
                        restaurant.setSubdomain("demo");
                        restaurant.setDescription("A demo restaurant for testing the multi-tenant platform");
                        restaurant.setEmail("demo@kitchenlab.com");
                        restaurant.setIsActive(true);
                        restaurant.setAllowsOnlineOrders(true);
                        restaurant.setAllowsDineIn(true);
                        restaurant.setAllowsTakeout(true);
                        restaurant.setIsDeleted(false);
                        
                        restaurant = restaurantRepository.save(restaurant);
                        log.info("Created default restaurant: {}", restaurant.getName());
                        return restaurant;
                    });
        } catch (Exception e) {
            log.error("Error creating default restaurant: {}", e.getMessage());
            throw e;
        }
    }

    private void createDefaultAdminUser(UUID restaurantId) {
        try {
            if (!adminRepository.findByUsername("admin").isPresent()) {
                Admin admin = new Admin();
                admin.setRestaurantId(restaurantId);
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@kitchenlab.com");
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setRole(Admin.AdminRole.SUPER_ADMIN);
                admin.setIsEnabled(true);
                admin.setIsDeleted(false);
                
                adminRepository.save(admin);
                log.info("Created default admin user: admin/admin123");
            } else {
                log.info("Admin user already exists");
            }
        } catch (Exception e) {
            log.error("Error creating default admin user: {}", e.getMessage());
            throw e;
        }
    }
}
