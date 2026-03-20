package com.binarycliff.kitchenlab.config;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.auth.repository.AdminRepository;
import com.binarycliff.kitchenlab.tenant.entity.Restaurant;
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
 * Temporary controller to initialize data for testing.
 */
@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
@Slf4j
public class SetupController {

    private final AdminRepository adminRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/init-data")
    public ResponseEntity<String> initializeData() {
        try {
            // Create Demo Restaurant if not exists
            Restaurant demoRestaurant = restaurantRepository.findBySubdomain("demo")
                    .orElseGet(() -> createRestaurant("Demo Restaurant", "demo", "A demo restaurant for testing the multi-tenant platform", "demo@kitchenlab.com"));
            
            // Create super admin if not exists
            if (!adminRepository.findByUsername("admin").isPresent()) {
                createAdminUser(demoRestaurant.getId(), "admin", "admin123", "admin@kitchenlab.com", "System", "Administrator", Admin.AdminRole.SUPER_ADMIN);
            }

            // Create Pizza Palace
            Restaurant pizzaRestaurant = restaurantRepository.findBySubdomain("pizza")
                    .orElseGet(() -> createRestaurant("Pizza Palace", "pizza", "Best pizza in town", "pizza@kitchenlab.com"));
            
            if (!adminRepository.findByUsername("pizza_admin").isPresent()) {
                createAdminUser(pizzaRestaurant.getId(), "pizza_admin", "admin123", "admin@pizza.com", "Pizza", "Admin", Admin.AdminRole.RESTAURANT_ADMIN);
            }

            // Create Burger Barn
            Restaurant burgerRestaurant = restaurantRepository.findBySubdomain("burger")
                    .orElseGet(() -> createRestaurant("Burger Barn", "burger", "Gourmet burgers and fries", "burger@kitchenlab.com"));
            
            if (!adminRepository.findByUsername("burger_admin").isPresent()) {
                createAdminUser(burgerRestaurant.getId(), "burger_admin", "admin123", "admin@burger.com", "Burger", "Admin", Admin.AdminRole.RESTAURANT_ADMIN);
            }

            // Create Sushi Spot
            Restaurant sushiRestaurant = restaurantRepository.findBySubdomain("sushi")
                    .orElseGet(() -> createRestaurant("Sushi Spot", "sushi", "Fresh Japanese cuisine", "sushi@kitchenlab.com"));
            
            if (!adminRepository.findByUsername("sushi_admin").isPresent()) {
                createAdminUser(sushiRestaurant.getId(), "sushi_admin", "admin123", "admin@sushi.com", "Sushi", "Admin", Admin.AdminRole.RESTAURANT_ADMIN);
            }

            // Create Manager users for each restaurant
            if (!adminRepository.findByUsername("pizza_manager").isPresent()) {
                createAdminUser(pizzaRestaurant.getId(), "pizza_manager", "admin123", "manager@pizza.com", "Pizza", "Manager", Admin.AdminRole.MANAGER);
            }
            if (!adminRepository.findByUsername("burger_manager").isPresent()) {
                createAdminUser(burgerRestaurant.getId(), "burger_manager", "admin123", "manager@burger.com", "Burger", "Manager", Admin.AdminRole.MANAGER);
            }
            if (!adminRepository.findByUsername("sushi_manager").isPresent()) {
                createAdminUser(sushiRestaurant.getId(), "sushi_manager", "admin123", "manager@sushi.com", "Sushi", "Manager", Admin.AdminRole.MANAGER);
            }

            // Create Staff users for each restaurant
            if (!adminRepository.findByUsername("pizza_staff").isPresent()) {
                createAdminUser(pizzaRestaurant.getId(), "pizza_staff", "admin123", "staff@pizza.com", "Pizza", "Staff", Admin.AdminRole.STAFF);
            }
            if (!adminRepository.findByUsername("burger_staff").isPresent()) {
                createAdminUser(burgerRestaurant.getId(), "burger_staff", "admin123", "staff@burger.com", "Burger", "Staff", Admin.AdminRole.STAFF);
            }
            if (!adminRepository.findByUsername("sushi_staff").isPresent()) {
                createAdminUser(sushiRestaurant.getId(), "sushi_staff", "admin123", "staff@sushi.com", "Sushi", "Staff", Admin.AdminRole.STAFF);
            }

            return ResponseEntity.ok("""
                Multi-tenant data created successfully!
                
                Login Credentials:
                - Super Admin: admin / admin123 (can see all restaurants)
                - Pizza Palace: pizza_admin / admin123 (can only see Pizza Palace)
                - Burger Barn: burger_admin / admin123 (can only see Burger Barn)  
                - Sushi Spot: sushi_admin / admin123 (can only see Sushi Spot)
                - Pizza Manager: pizza_manager / admin123 (Pizza Palace manager)
                - Burger Manager: burger_manager / admin123 (Burger Barn manager)
                - Sushi Manager: sushi_manager / admin123 (Sushi Spot manager)
                - Pizza Staff: pizza_staff / admin123 (Pizza Palace staff)
                - Burger Staff: burger_staff / admin123 (Burger Barn staff)
                - Sushi Staff: sushi_staff / admin123 (Sushi Spot staff)
                
                Test by logging in at: http://localhost:8080/auth/login
                Each user should only see their own restaurant data based on their role.
                """);
        } catch (Exception e) {
            log.error("Error initializing data", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    private Restaurant createRestaurant(String name, String subdomain, String description, String email) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setSubdomain(subdomain);
        restaurant.setDescription(description);
        restaurant.setEmail(email);
        restaurant.setIsActive(true);
        restaurant.setAllowsOnlineOrders(true);
        restaurant.setAllowsDineIn(true);
        restaurant.setAllowsTakeout(true);
        restaurant.setIsDeleted(false);
        
        restaurant = restaurantRepository.save(restaurant);
        log.info("Created restaurant: {}", restaurant.getName());
        return restaurant;
    }
    
    private void createAdminUser(UUID restaurantId, String username, String password, String email, String firstName, String lastName, Admin.AdminRole role) {
        Admin admin = new Admin();
        admin.setRestaurantId(restaurantId);
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEmail(email);
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setRole(role);
        admin.setIsEnabled(true);
        admin.setIsDeleted(false);
        
        adminRepository.save(admin);
        log.info("Created admin user: {}/{}", username, password);
    }
}
