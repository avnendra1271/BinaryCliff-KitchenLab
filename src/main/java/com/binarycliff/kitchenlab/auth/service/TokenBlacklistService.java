package com.binarycliff.kitchenlab.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for managing blacklisted tokens (logout functionality).
 * Uses in-memory storage for demo purposes. In production, use Redis or database.
 */
@Slf4j
@Service
public class TokenBlacklistService {
    
    private final ConcurrentMap<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();
    
    /**
     * Add token to blacklist.
     */
    public void blacklistToken(String token) {
        blacklistedTokens.put(token, LocalDateTime.now());
        log.info("Token blacklisted: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
    }
    
    /**
     * Check if token is blacklisted.
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }
    
    /**
     * Remove expired tokens from blacklist (cleanup).
     */
    public void cleanupExpiredTokens() {
        // This is a simple cleanup - in production, you'd want to check actual token expiration
        // For now, we'll remove tokens older than 24 hours
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        log.info("Token blacklist cleanup completed. Current size: {}", blacklistedTokens.size());
    }
    
    /**
     * Get current blacklist size.
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
}
