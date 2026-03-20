package com.binarycliff.kitchenlab.tenant.repository;

import com.binarycliff.kitchenlab.tenant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Restaurant entities.
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    
    Optional<Restaurant> findBySubdomain(String subdomain);
    
    Optional<Restaurant> findBySubdomainAndIsActive(String subdomain, Boolean isActive);
    
    boolean existsBySubdomain(String subdomain);
    
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true ORDER BY r.name")
    Page<Restaurant> findActiveRestaurants(Pageable pageable);
    
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.allowsOnlineOrders = true ORDER BY r.name")
    Page<Restaurant> findActiveRestaurantsWithOnlineOrders(Pageable pageable);
    
    @Query("SELECT COUNT(r) FROM Restaurant r WHERE r.isActive = true")
    long countActiveRestaurants();
    
    @Query("SELECT r FROM Restaurant r WHERE r.name LIKE %:name% AND r.isActive = true")
    Page<Restaurant> findActiveRestaurantsByNameContaining(@Param("name") String name, Pageable pageable);
}
