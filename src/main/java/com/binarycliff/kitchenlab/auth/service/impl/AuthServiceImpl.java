package com.binarycliff.kitchenlab.auth.service.impl;

import com.binarycliff.kitchenlab.auth.dto.*;
import com.binarycliff.kitchenlab.auth.entity.Admin;
import com.binarycliff.kitchenlab.auth.entity.EmailVerificationToken;
import com.binarycliff.kitchenlab.auth.entity.PasswordResetToken;
import com.binarycliff.kitchenlab.auth.entity.UserSession;
import com.binarycliff.kitchenlab.auth.repository.AdminRepository;
import com.binarycliff.kitchenlab.auth.repository.EmailVerificationTokenRepository;
import com.binarycliff.kitchenlab.auth.repository.PasswordResetTokenRepository;
import com.binarycliff.kitchenlab.auth.repository.UserSessionRepository;
import com.binarycliff.kitchenlab.auth.service.*;
import com.binarycliff.kitchenlab.auth.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of AuthService interface.
 * Provides authentication and admin management functionality with proper exception handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SessionService sessionService;
    private final RateLimitingService rateLimitingService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());
        
        // Validate request
        if (!validateLoginRequest(request)) {
            throw new ValidationException("Invalid login request");
        }
        
        String identifier = request.getUsername().toLowerCase();
        
        // Check rate limiting
        if (rateLimitingService.isLockedOut(identifier)) {
            long remainingMinutes = rateLimitingService.getRemainingLockoutMinutes(identifier);
            throw new AccountLockedException(remainingMinutes);
        }
        
        // Find user
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    rateLimitingService.recordFailedAttempt(identifier);
                    return new UserNotFoundException("Invalid username or password");
                });
        
        // Validate user status
        validateUserStatus(admin, identifier);
        
        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            rateLimitingService.recordFailedAttempt(identifier);
            throw new UserNotFoundException("Invalid username or password");
        }

        // Successful login - clear failed attempts and update login info
        rateLimitingService.recordSuccessfulAttempt(identifier);
        updateLoginInfo(admin);

        // Generate tokens and create session
        String accessToken = jwtService.generateAccessToken(
                admin.getId(), admin.getUsername(), admin.getEmail(), admin.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(admin.getId());
        
        createSession(admin.getId(), accessToken, refreshToken);

        return buildLoginResponse(admin, accessToken, refreshToken);
    }

    @Override
    public void logout(String token) {
        log.info("Logging out user with token");
        if (token != null && !token.trim().isEmpty()) {
            tokenBlacklistService.blacklistToken(token);
            sessionService.deactivateSessionByAccessToken(token);
        }
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        // Validate refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token", "INVALID_REFRESH_TOKEN");
        }
        
        // Check if token is blacklisted
        if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
            throw new AuthenticationException("Refresh token is blacklisted", "TOKEN_BLACKLISTED");
        }
        
        // Extract and validate user
        UUID userId = jwtService.extractUserId(refreshToken);
        Admin admin = adminRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        validateUserStatus(admin, userId.toString());
        
        // Generate new tokens and update session
        String newAccessToken = jwtService.generateAccessToken(
                admin.getId(), admin.getUsername(), admin.getEmail(), admin.getRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(admin.getId());
        
        updateExistingSession(refreshToken, newAccessToken, newRefreshToken);
        tokenBlacklistService.blacklistToken(refreshToken);
        
        return buildLoginResponse(admin, newAccessToken, newRefreshToken);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }
            
            // Check if token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.debug("Token is blacklisted: {}", token.substring(0, Math.min(10, token.length())));
                return false;
            }
            
            // Validate JWT token
            jwtService.validateToken(token);
            return !jwtService.isTokenExpired(token);
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Admin createAdmin(CreateAdminRequest request) {
        log.info("Creating new admin: {}", request.getUsername());
        
        // Validate request
        validateCreateAdminRequest(request);
        
        Admin admin = Admin.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhoneNumber())
                .role(request.getRole())
                .isEnabled(request.getIsActive())
                .build();

        Admin savedAdmin = adminRepository.save(admin);
        
        // Send welcome email if enabled
        if (emailService.isEmailEnabled()) {
            sendWelcomeEmail(savedAdmin);
        }
        
        return savedAdmin;
    }

    @Override
    public Optional<Admin> getAdminById(UUID id) {
        return adminRepository.findById(id);
    }

    @Override
    public Optional<Admin> getAdminByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    @Override
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    public List<Admin> getActiveAdmins() {
        return adminRepository.findActiveAdmins();
    }

    @Override
    public Admin updateAdmin(UUID id, UpdateAdminRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));
        
        // Update fields if provided
        updateAdminFields(admin, request);
        
        return adminRepository.save(admin);
    }

    @Override
    public void deleteAdmin(UUID id) {
        adminRepository.deleteById(id);
    }

    @Override
    public void activateAdmin(UUID id) {
        Optional<Admin> adminOpt = adminRepository.findById(id);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setIsEnabled(true);
            adminRepository.save(admin);
        }
    }

    @Override
    public void deactivateAdmin(UUID id) {
        Optional<Admin> adminOpt = adminRepository.findById(id);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setIsEnabled(false);
            adminRepository.save(admin);
        }
    }

    @Override
    public void changePassword(UUID adminId, String currentPassword, String newPassword) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));
        
        // Validate current password
        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            throw new AuthenticationException("Current password is incorrect", "INVALID_PASSWORD");
        }
        
        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters");
        }
        
        admin.setPassword(passwordEncoder.encode(newPassword));
        admin.setPasswordChangedAt(LocalDateTime.now());
        adminRepository.save(admin);
        
        log.info("Password changed successfully for admin: {}", admin.getUsername());
    }

    @Override
    public void resetPassword(String email) {
        log.info("Password reset requested for email: {}", email);
        
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isEmpty()) {
            // Don't reveal that email doesn't exist for security
            log.info("Password reset requested for non-existent email: {}", email);
            return;
        }
        
        Admin admin = adminOpt.get();
        if (!admin.getIsEnabled()) {
            log.info("Password reset requested for disabled account: {}", email);
            return;
        }
        
        // Invalidate existing tokens
        passwordResetTokenRepository.findByAdminIdAndIsUsed(admin.getId(), false)
                .ifPresent(token -> {
                    token.setIsUsed(true);
                    passwordResetTokenRepository.save(token);
                });
        
        // Create new reset token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .adminId(admin.getId())
                .build();
        
        resetToken = passwordResetTokenRepository.save(resetToken);
        
        // Send reset email
        emailService.sendPasswordResetEmail(email, resetToken.getToken());
        
        log.info("Password reset token created for email: {}", email);
    }

    @Override
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public void confirmPasswordReset(String token, String newPassword) {
        log.info("Confirming password reset with token: {}", token);
        
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            throw new ValidationException("Invalid reset token");
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        // Check if token is used
        if (resetToken.getIsUsed()) {
            throw new ValidationException("Reset token has already been used");
        }
        
        // Check if token is expired
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Reset token has expired");
        }
        
        // Find admin
        Optional<Admin> adminOpt = adminRepository.findById(resetToken.getAdminId());
        if (adminOpt.isEmpty()) {
            throw new ValidationException("Admin not found");
        }
        
        Admin admin = adminOpt.get();
        
        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }
        
        // Update password
        admin.setPassword(passwordEncoder.encode(newPassword));
        admin.setPasswordChangedAt(LocalDateTime.now());
        adminRepository.save(admin);
        
        // Mark token as used
        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);
        
        // Revoke all sessions for this admin
        sessionService.deactivateAllSessions(admin.getId());
        
        log.info("Password reset completed for admin: {}", admin.getUsername());
    }

    @Override
    public boolean validatePasswordResetToken(String token) {
        log.info("Validating password reset token: {}", token);
        
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        // Check if token is used or expired
        return !resetToken.getIsUsed() && resetToken.getExpiresAt().isAfter(LocalDateTime.now());
    }

    /**
     * Validates create admin request.
     */
    private void validateCreateAdminRequest(CreateAdminRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }
        if (adminRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already exists");
        }
        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }
    }

    /**
     * Updates admin fields from request.
     */
    private void updateAdminFields(Admin admin, UpdateAdminRequest request) {
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (!request.getUsername().equals(admin.getUsername()) && 
                adminRepository.existsByUsername(request.getUsername())) {
                throw new ValidationException("Username already exists");
            }
            admin.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(admin.getEmail()) && 
                adminRepository.existsByEmail(request.getEmail())) {
                throw new ValidationException("Email already exists");
            }
            admin.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            admin.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            admin.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            admin.setPhone(request.getPhoneNumber());
        }
        if (request.getRole() != null) {
            admin.setRole(request.getRole());
        }
        if (request.getIsActive() != null) {
            admin.setIsEnabled(request.getIsActive());
        }
        if (request.getProfileImageUrl() != null) {
            admin.setProfileImageUrl(request.getProfileImageUrl());
        }
    }

    /**
     * Sends welcome email to new admin.
     */
    private void sendWelcomeEmail(Admin admin) {
        try {
            // Create email verification token
            EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                    .adminId(admin.getId())
                    .email(admin.getEmail())
                    .build();
            
            emailVerificationTokenRepository.save(verificationToken);
            emailService.sendEmailVerification(admin.getEmail(), admin.getId(), verificationToken.getToken());
            emailService.sendWelcomeEmail(admin.getEmail(), admin.getUsername());
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", admin.getEmail(), e.getMessage());
        }
    }

    @Override
    public Admin updateProfile(UUID adminId, UpdateProfileRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));
        
        // Update profile fields
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            admin.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            admin.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(admin.getEmail()) && 
                adminRepository.existsByEmail(request.getEmail())) {
                throw new ValidationException("Email already exists");
            }
            admin.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            admin.setPhone(request.getPhoneNumber());
        }
        if (request.getProfileImageUrl() != null) {
            admin.setProfileImageUrl(request.getProfileImageUrl());
        }

        return adminRepository.save(admin);
    }

    @Override
    public void updateProfileImage(UUID adminId, String imageUrl) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));
        
        admin.setProfileImageUrl(imageUrl);
        adminRepository.save(admin);
    }

    @Override
    public void recordLogin(UUID adminId, String ipAddress) {
        adminRepository.findById(adminId).ifPresent(admin -> {
            admin.setLastLoginAt(LocalDateTime.now());
            admin.setLastLoginIp(ipAddress);
            adminRepository.save(admin);
        });
    }

    @Override
    public List<String> getActiveSessions(UUID adminId) {
        return sessionService.getActiveSessions(adminId).stream()
                .map(session -> session.getId())
                .toList();
    }

    @Override
    public void revokeSession(String sessionId) {
        sessionService.deactivateSession(sessionId);
    }

    @Override
    public void revokeAllSessions(UUID adminId) {
        sessionService.deactivateAllSessions(adminId);
    }

    @Override
    public boolean validateLoginRequest(LoginRequest request) {
        return request != null && 
               request.getUsername() != null && !request.getUsername().trim().isEmpty() &&
               request.getPassword() != null && !request.getPassword().trim().isEmpty();
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !adminRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailAvailable(String email, UUID excludeId) {
        if (excludeId != null) {
            return !adminRepository.existsByEmailAndIdNot(email, excludeId);
        }
        return !adminRepository.existsByEmail(email);
    }

    @Override
    public List<Admin> getAdminsByRole(Admin.AdminRole role) {
        return adminRepository.findByRole(role);
    }

    // ==================== Helper Methods ====================

    /**
     * Validates user status and throws appropriate exceptions.
     */
    private void validateUserStatus(Admin admin, String identifier) {
        if (!admin.getIsEnabled()) {
            rateLimitingService.recordFailedAttempt(identifier);
            throw new AuthenticationException("Account is deactivated", "ACCOUNT_DEACTIVATED");
        }
    }

    /**
     * Updates login information for admin user.
     */
    private void updateLoginInfo(Admin admin) {
        admin.setLastLoginAt(LocalDateTime.now());
        admin.setLastLoginIp("unknown"); // TODO: Extract from request
        adminRepository.save(admin);
    }

    /**
     * Creates a new user session.
     */
    private void createSession(UUID adminId, String accessToken, String refreshToken) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plus(1, ChronoUnit.HOURS);
        sessionService.createSession(
                adminId, sessionId, accessToken, refreshToken, 
                "unknown", null, expiresAt); // TODO: Extract IP and user agent from request
    }

    /**
     * Updates existing session with new tokens.
     */
    private void updateExistingSession(String oldRefreshToken, String newAccessToken, String newRefreshToken) {
        sessionService.findByRefreshToken(oldRefreshToken)
                .ifPresent(session -> {
                    session.setAccessToken(newAccessToken);
                    session.setRefreshToken(newRefreshToken);
                    session.setExpiresAt(LocalDateTime.now().plus(1, ChronoUnit.HOURS));
                    session.setLastAccessedAt(LocalDateTime.now());
                    sessionService.updateLastAccessed(session.getId());
                });
    }

    /**
     * Builds login response with admin data.
     */
    private LoginResponse buildLoginResponse(Admin admin, String accessToken, String refreshToken) {
        LoginResponse.AdminResponse adminResponse = LoginResponse.AdminResponse.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .fullName(admin.getFirstName() + " " + admin.getLastName())
                .profileImageUrl(admin.getProfileImageUrl())
                .role(admin.getRole())
                .permissions(getPermissionsForRole(admin.getRole()))
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .admin(adminResponse)
                .build();
    }

    /**
     * Gets permissions based on user role.
     */
    private List<String> getPermissionsForRole(Admin.AdminRole role) {
        return switch (role) {
            case SUPER_ADMIN -> List.of("USER_READ", "USER_WRITE", "USER_DELETE",
                    "ROLE_READ", "ROLE_WRITE", "ROLE_DELETE",
                    "MENU_READ", "MENU_WRITE", "MENU_DELETE",
                    "ORDER_READ", "ORDER_WRITE", "ORDER_DELETE",
                    "SYSTEM_CONFIG", "SYSTEM_ADMIN");
            case RESTAURANT_ADMIN -> List.of("MENU_READ", "MENU_WRITE", "MENU_DELETE",
                    "ORDER_READ", "ORDER_WRITE", "ORDER_DELETE",
                    "STAFF_MANAGEMENT");
            case MANAGER -> List.of("MENU_READ", "MENU_WRITE",
                    "ORDER_READ", "ORDER_WRITE", "ORDER_DELETE");
            case STAFF -> List.of("MENU_READ", "ORDER_READ", "ORDER_WRITE");
            default -> List.of();
        };
    }

}
