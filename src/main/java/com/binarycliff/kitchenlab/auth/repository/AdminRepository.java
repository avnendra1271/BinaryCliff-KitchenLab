package com.binarycliff.kitchenlab.auth.repository;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Admin entity operations.
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {
    
    Optional<Admin> findByUsername(String username);
    
    Optional<Admin> findByUsernameAndRestaurantId(String username, UUID restaurantId);
    
    Optional<Admin> findByEmail(String email);
    
    Optional<Admin> findByEmailAndRestaurantId(String email, UUID restaurantId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdNot(String email, UUID id);
    
    boolean existsByUsernameAndRestaurantId(String username, UUID restaurantId);
    
    List<Admin> findByRestaurantId(UUID restaurantId);
    
    @Query("SELECT a FROM Admin a WHERE a.restaurantId = :restaurantId AND a.isEnabled = true AND a.isDeleted = false")
    List<Admin> findActiveAdminsByRestaurant(@Param("restaurantId") UUID restaurantId);
    
    @Query("SELECT a FROM Admin a WHERE a.isEnabled = true AND a.isDeleted = false")
    List<Admin> findActiveAdmins();
    
    @Query("SELECT a FROM Admin a WHERE a.role = :role AND a.isEnabled = true AND a.isDeleted = false")
    List<Admin> findByRole(@Param("role") Admin.AdminRole role);
    
    @Query("SELECT a FROM Admin a WHERE a.role = :role AND a.restaurantId = :restaurantId AND a.isEnabled = true AND a.isDeleted = false")
    List<Admin> findByRoleAndRestaurant(@Param("role") Admin.AdminRole role, @Param("restaurantId") UUID restaurantId);
    
    @Query("SELECT a FROM Admin a WHERE a.lastLoginAt < :date AND a.isEnabled = true")
    List<Admin> findInactiveAdminsSince(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.isEnabled = true AND a.isDeleted = false")
    Long countActiveAdmins();
    
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.restaurantId = :restaurantId AND a.isEnabled = true AND a.isDeleted = false")
    Long countActiveAdminsByRestaurant(@Param("restaurantId") UUID restaurantId);
}
