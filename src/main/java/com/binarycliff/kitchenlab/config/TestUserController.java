package com.binarycliff.kitchenlab.config;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.auth.repository.AdminRepository;
import com.binarycliff.kitchenlab.tenant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller to add RESTAURANT_ADMIN user for testing.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestUserController {

    private final AdminRepository adminRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/add-restaurant-admin")
    public ResponseEntity<String> addRestaurantAdmin() {
        try {
            // Get the existing restaurant
            var restaurant = restaurantRepository.findBySubdomain("demo")
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            // Create restaurant admin user
            Admin restaurantAdmin = new Admin();
            restaurantAdmin.setRestaurantId(restaurant.getId());
            restaurantAdmin.setUsername("restaurant");
            restaurantAdmin.setPassword(passwordEncoder.encode("restaurant123"));
            restaurantAdmin.setEmail("restaurant@kitchenlab.com");
            restaurantAdmin.setFirstName("Restaurant");
            restaurantAdmin.setLastName("Admin");
            restaurantAdmin.setRole(Admin.AdminRole.RESTAURANT_ADMIN);
            restaurantAdmin.setIsEnabled(true);
            restaurantAdmin.setIsDeleted(false);
            
            adminRepository.save(restaurantAdmin);
            log.info("Created restaurant admin user: restaurant/restaurant123");

            return ResponseEntity.ok("Restaurant admin created successfully! Use restaurant/restaurant123 to login.");
        } catch (Exception e) {
            log.error("Error creating restaurant admin", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
