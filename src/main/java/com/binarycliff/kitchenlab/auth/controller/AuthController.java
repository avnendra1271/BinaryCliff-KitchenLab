package com.binarycliff.kitchenlab.auth.controller;

import com.binarycliff.kitchenlab.auth.dto.LoginRequest;
import com.binarycliff.kitchenlab.auth.dto.LoginResponse;
import com.binarycliff.kitchenlab.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Authentication operations.
 * Provides endpoints for login, logout, and user management.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Authenticate user and return JWT tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Logout user and invalidate token.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.replace("Bearer ", ""));
        return ResponseEntity.ok().build();
    }
    
    /**
     * Refresh access token using refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if username is available.
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Boolean> checkUsernameAvailability(@PathVariable String username) {
        boolean available = authService.isUsernameAvailable(username);
        return ResponseEntity.ok(available);
    }
    
    /**
     * Check if email is available.
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailAvailability(@PathVariable String email) {
        boolean available = authService.isEmailAvailable(email, null);
        return ResponseEntity.ok(available);
    }
    
    /**
     * Verify JWT token validity.
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String token) {
        try {
            String cleanedToken = token.replace("Bearer ", "");
            boolean isValid = authService.validateToken(cleanedToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "Token is valid" : "Token is invalid");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Token verification failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * Reset password for given email.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        authService.resetPassword(email);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset link has been sent to your email");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Confirm password reset with token.
     */
    @PostMapping("/confirm-reset-password")
    public ResponseEntity<Map<String, String>> confirmPasswordReset(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        
        if (token == null || token.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Reset token is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "New password is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        authService.confirmPasswordReset(token, newPassword);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password has been reset successfully");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Validate password reset token.
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Object>> validateResetToken(@RequestParam String token) {
        boolean isValid = authService.validatePasswordResetToken(token);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("message", isValid ? "Token is valid" : "Token is invalid or expired");
        
        return ResponseEntity.ok(response);
    }
}
