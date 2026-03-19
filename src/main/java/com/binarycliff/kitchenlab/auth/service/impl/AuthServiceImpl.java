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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
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
 * Provides authentication and admin management functionality.
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
        
        // Check rate limiting
        String identifier = request.getUsername().toLowerCase();
        if (rateLimitingService.isLockedOut(identifier)) {
            long remainingMinutes = rateLimitingService.getRemainingLockoutMinutes(identifier);
            throw new BadCredentialsException("Account locked. Try again in " + remainingMinutes + " minutes.");
        }
        
        Optional<Admin> adminOpt = adminRepository.findByUsername(request.getUsername());
        if (adminOpt.isEmpty()) {
            rateLimitingService.recordFailedAttempt(identifier);
            throw new BadCredentialsException("Invalid username or password");
        }

        Admin admin = adminOpt.get();
        
        if (!admin.getIsEnabled()) {
            rateLimitingService.recordFailedAttempt(identifier);
            throw new BadCredentialsException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            rateLimitingService.recordFailedAttempt(identifier);
            throw new BadCredentialsException("Invalid username or password");
        }

        // Successful login - clear failed attempts
        rateLimitingService.recordSuccessfulAttempt(identifier);

        // Update last login
        admin.setLastLoginAt(LocalDateTime.now());
        admin.setLastLoginIp("unknown");
        adminRepository.save(admin);

        // Generate proper JWT tokens
        String accessToken = jwtService.generateAccessToken(
            admin.getId(), 
            admin.getUsername(), 
            admin.getEmail(), 
            admin.getRole().name()
        );
        String refreshToken = jwtService.generateRefreshToken(admin.getId());
        
        // Create session
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plus(1, ChronoUnit.HOURS);
        sessionService.createSession(
            admin.getId(), 
            sessionId, 
            accessToken, 
            refreshToken, 
            "unknown", 
            null, 
            expiresAt
        );

        LoginResponse.AdminResponse adminResponse = LoginResponse.AdminResponse.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .fullName(admin.getFirstName() + " " + admin.getLastName())
                .profileImageUrl(admin.getProfileImageUrl())
                .role(admin.getRole())
                .permissions(List.of("READ", "WRITE", "ADMIN"))
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .admin(adminResponse)
                .build();
    }

    @Override
    public void logout(String token) {
        log.info("Logging out user with token");
        if (token != null && !token.isEmpty()) {
            tokenBlacklistService.blacklistToken(token);
            sessionService.deactivateSessionByAccessToken(token);
        }
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        // Validate refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        
        // Check if token is blacklisted
        if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
            throw new BadCredentialsException("Refresh token is blacklisted");
        }
        
        // Extract user info from refresh token
        UUID userId = jwtService.extractUserId(refreshToken);
        Optional<Admin> adminOpt = adminRepository.findById(userId);
        
        if (adminOpt.isEmpty()) {
            throw new BadCredentialsException("User not found");
        }
        
        Admin admin = adminOpt.get();
        if (!admin.getIsEnabled()) {
            throw new BadCredentialsException("Account is deactivated");
        }
        
        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(
            admin.getId(), 
            admin.getUsername(), 
            admin.getEmail(), 
            admin.getRole().name()
        );
        String newRefreshToken = jwtService.generateRefreshToken(admin.getId());
        
        // Update session
        Optional<UserSession> sessionOpt = sessionService.findByRefreshToken(refreshToken);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setAccessToken(newAccessToken);
            session.setRefreshToken(newRefreshToken);
            session.setExpiresAt(LocalDateTime.now().plus(1, ChronoUnit.HOURS));
            session.setLastAccessedAt(LocalDateTime.now());
            sessionService.updateLastAccessed(session.getId());
        }
        
        // Blacklist old refresh token
        tokenBlacklistService.blacklistToken(refreshToken);
        
        LoginResponse.AdminResponse adminResponse = LoginResponse.AdminResponse.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .fullName(admin.getFirstName() + " " + admin.getLastName())
                .profileImageUrl(admin.getProfileImageUrl())
                .role(admin.getRole())
                .permissions(List.of("READ", "WRITE", "ADMIN"))
                .build();
        
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .admin(adminResponse)
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            // Check if token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
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
        
        if (adminRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

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
        
        // Send welcome email
        emailService.sendWelcomeEmail(savedAdmin.getEmail(), savedAdmin.getUsername());
        
        // Create email verification token
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .adminId(savedAdmin.getId())
                .email(savedAdmin.getEmail())
                .build();
        
        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendEmailVerification(savedAdmin.getEmail(), savedAdmin.getId(), verificationToken.getToken());
        
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
        Optional<Admin> adminOpt = adminRepository.findById(id);
        if (adminOpt.isEmpty()) {
            throw new IllegalArgumentException("Admin not found");
        }

        Admin admin = adminOpt.get();
        
        if (request.getUsername() != null) {
            admin.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
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
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            throw new IllegalArgumentException("Admin not found");
        }

        Admin admin = adminOpt.get();
        
        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
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
    public Admin updateProfile(UUID adminId, UpdateProfileRequest request) {
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            throw new IllegalArgumentException("Admin not found");
        }

        Admin admin = adminOpt.get();
        
        if (request.getFirstName() != null) {
            admin.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            admin.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
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
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setProfileImageUrl(imageUrl);
            adminRepository.save(admin);
        }
    }

    @Override
    public void recordLogin(UUID adminId, String ipAddress) {
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setLastLoginAt(LocalDateTime.now());
            adminRepository.save(admin);
        }
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
    public boolean hasPermission(UUID adminId, String permission) {
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            return false;
        }
        
        Admin admin = adminOpt.get();
        // Simplified permission check
        return admin.getIsEnabled() && admin.getRole() == Admin.AdminRole.SUPER_ADMIN;
    }

    @Override
    public boolean hasRole(UUID adminId, Admin.AdminRole role) {
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        return adminOpt.map(admin -> admin.getRole() == role).orElse(false);
    }

    @Override
    public List<Admin> getAdminsByRole(Admin.AdminRole role) {
        return adminRepository.findByRole(role);
    }

}
