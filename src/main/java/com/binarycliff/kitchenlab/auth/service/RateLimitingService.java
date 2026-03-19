package com.binarycliff.kitchenlab.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for rate limiting login attempts and account lockout.
 */
@Slf4j
@Service
public class RateLimitingService {
    
    private final ConcurrentMap<String, LoginAttemptInfo> loginAttempts = new ConcurrentHashMap<>();
    
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 30;
    private static final long ATTEMPT_WINDOW_MINUTES = 15;
    
    /**
     * Record a failed login attempt.
     */
    public void recordFailedAttempt(String identifier) {
        String key = identifier.toLowerCase();
        LoginAttemptInfo info = loginAttempts.computeIfAbsent(key, k -> new LoginAttemptInfo());
        
        info.incrementAttempt();
        log.info("Failed login attempt {} for identifier: {}", info.getAttemptCount(), key);
        
        if (info.getAttemptCount() >= MAX_ATTEMPTS) {
            info.setLockoutTime(LocalDateTime.now());
            log.warn("Account locked for identifier: {} due to too many failed attempts", key);
        }
    }
    
    /**
     * Record a successful login attempt (resets the counter).
     */
    public void recordSuccessfulAttempt(String identifier) {
        String key = identifier.toLowerCase();
        loginAttempts.remove(key);
        log.info("Successful login recorded for identifier: {}", key);
    }
    
    /**
     * Check if the identifier is currently locked out.
     */
    public boolean isLockedOut(String identifier) {
        String key = identifier.toLowerCase();
        LoginAttemptInfo info = loginAttempts.get(key);
        
        if (info == null || info.getLockoutTime() == null) {
            return false;
        }
        
        // Check if lockout has expired
        if (info.getLockoutTime().plusMinutes(LOCKOUT_DURATION_MINUTES).isBefore(LocalDateTime.now())) {
            loginAttempts.remove(key);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get remaining lockout time in minutes.
     */
    public long getRemainingLockoutMinutes(String identifier) {
        String key = identifier.toLowerCase();
        LoginAttemptInfo info = loginAttempts.get(key);
        
        if (info == null || info.getLockoutTime() == null) {
            return 0;
        }
        
        LocalDateTime lockoutEnd = info.getLockoutTime().plusMinutes(LOCKOUT_DURATION_MINUTES);
        if (lockoutEnd.isBefore(LocalDateTime.now())) {
            return 0;
        }
        
        return java.time.Duration.between(LocalDateTime.now(), lockoutEnd).toMinutes();
    }
    
    /**
     * Get remaining attempts before lockout.
     */
    public int getRemainingAttempts(String identifier) {
        String key = identifier.toLowerCase();
        LoginAttemptInfo info = loginAttempts.get(key);
        
        if (info == null) {
            return MAX_ATTEMPTS;
        }
        
        // Reset attempts if window has expired
        if (info.getLastAttemptTime().plusMinutes(ATTEMPT_WINDOW_MINUTES).isBefore(LocalDateTime.now())) {
            loginAttempts.remove(key);
            return MAX_ATTEMPTS;
        }
        
        return Math.max(0, MAX_ATTEMPTS - info.getAttemptCount());
    }
    
    /**
     * Clear all failed attempts for an identifier.
     */
    public void clearAttempts(String identifier) {
        String key = identifier.toLowerCase();
        loginAttempts.remove(key);
        log.info("Cleared login attempts for identifier: {}", key);
    }
    
    /**
     * Cleanup old attempts periodically.
     */
    public void cleanupOldAttempts() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(ATTEMPT_WINDOW_MINUTES);
        loginAttempts.entrySet().removeIf(entry -> {
            LoginAttemptInfo info = entry.getValue();
            boolean shouldRemove = info.getLastAttemptTime().isBefore(cutoff) && 
                                 (info.getLockoutTime() == null || 
                                  info.getLockoutTime().plusMinutes(LOCKOUT_DURATION_MINUTES).isBefore(LocalDateTime.now()));
            if (shouldRemove) {
                log.debug("Removed old login attempt record for: {}", entry.getKey());
            }
            return shouldRemove;
        });
    }
    
    /**
     * Inner class to track login attempt information.
     */
    private static class LoginAttemptInfo {
        private int attemptCount = 0;
        private LocalDateTime lastAttemptTime = LocalDateTime.now();
        private LocalDateTime lockoutTime;
        
        public void incrementAttempt() {
            this.attemptCount++;
            this.lastAttemptTime = LocalDateTime.now();
        }
        
        public int getAttemptCount() {
            return attemptCount;
        }
        
        public LocalDateTime getLastAttemptTime() {
            return lastAttemptTime;
        }
        
        public LocalDateTime getLockoutTime() {
            return lockoutTime;
        }
        
        public void setLockoutTime(LocalDateTime lockoutTime) {
            this.lockoutTime = lockoutTime;
        }
    }
}
