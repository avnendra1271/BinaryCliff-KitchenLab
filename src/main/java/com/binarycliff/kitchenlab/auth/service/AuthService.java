package com.binarycliff.kitchenlab.auth.service;

import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.auth.dto.LoginRequest;
import com.binarycliff.kitchenlab.auth.dto.LoginResponse;
import com.binarycliff.kitchenlab.auth.dto.AdminResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for Authentication operations.
 * Provides core authentication and admin management functionality.
 */
public interface AuthService {
    
    // Authentication operations
    LoginResponse login(LoginRequest request);
    void logout(String token);
    LoginResponse refreshToken(String refreshToken);
    boolean validateToken(String token);
    
    // Admin CRUD operations
    Admin createAdmin(com.binarycliff.kitchenlab.auth.dto.CreateAdminRequest request);
    Optional<Admin> getAdminById(UUID id);
    Optional<Admin> getAdminByUsername(String username);
    List<Admin> getAllAdmins();
    List<Admin> getActiveAdmins();
    Admin updateAdmin(UUID id, com.binarycliff.kitchenlab.auth.dto.UpdateAdminRequest request);
    void deleteAdmin(UUID id);
    void activateAdmin(UUID id);
    void deactivateAdmin(UUID id);
    
    // Password management
    void changePassword(UUID adminId, String currentPassword, String newPassword);
    void resetPassword(String email);
    void confirmPasswordReset(String token, String newPassword);
    boolean validatePasswordResetToken(String token);
    boolean validatePassword(String rawPassword, String encodedPassword);
    
    // Profile management
    Admin updateProfile(UUID adminId, com.binarycliff.kitchenlab.auth.dto.UpdateProfileRequest request);
    void updateProfileImage(UUID adminId, String imageUrl);
    
    // Session management
    void recordLogin(UUID adminId, String ipAddress);
    List<String> getActiveSessions(UUID adminId);
    void revokeSession(String sessionId);
    void revokeAllSessions(UUID adminId);
    
    // Validation
    boolean validateLoginRequest(LoginRequest request);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email, UUID excludeId);
    
    // Role-based queries
    List<Admin> getAdminsByRole(Admin.AdminRole role);
}
