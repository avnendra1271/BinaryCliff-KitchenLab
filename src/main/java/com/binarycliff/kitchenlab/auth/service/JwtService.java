package com.binarycliff.kitchenlab.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * Service for JWT token generation and validation.
 */
@Slf4j
@Service
public class JwtService {
    
    @Value("${app.jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:3600}")
    private long jwtExpiration;
    
    @Value("${app.jwt.refresh-expiration:604800}")
    private long refreshExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    /**
     * Generate JWT access token for user.
     */
    public String generateAccessToken(UUID userId, String username, String email, String role) {
        Instant now = Instant.now();
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("email", email)
                .claim("role", role)
                .claim("tokenType", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtExpiration, ChronoUnit.SECONDS)))
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Generate JWT refresh token.
     */
    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("tokenType", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshExpiration, ChronoUnit.SECONDS)))
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Validate JWT token and return claims.
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw new SecurityException("Token expired");
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw new SecurityException("Unsupported token");
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw new SecurityException("Malformed token");
        } catch (SecurityException e) {
            log.warn("JWT token signature validation failed: {}", e.getMessage());
            throw new SecurityException("Invalid token signature");
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid: {}", e.getMessage());
            throw new SecurityException("Invalid token");
        }
    }
    
    /**
     * Extract user ID from token.
     */
    public UUID extractUserId(String token) {
        Claims claims = validateToken(token);
        return UUID.fromString(claims.getSubject());
    }
    
    /**
     * Extract username from token.
     */
    public String extractUsername(String token) {
        Claims claims = validateToken(token);
        return claims.get("username", String.class);
    }
    
    /**
     * Extract email from token.
     */
    public String extractEmail(String token) {
        Claims claims = validateToken(token);
        return claims.get("email", String.class);
    }
    
    /**
     * Extract role from token.
     */
    public String extractRole(String token) {
        Claims claims = validateToken(token);
        return claims.get("role", String.class);
    }
    
    /**
     * Check if token is a refresh token.
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = validateToken(token);
            return "refresh".equals(claims.get("tokenType", String.class));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if token is an access token.
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = validateToken(token);
            return "access".equals(claims.get("tokenType", String.class));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get token expiration time.
     */
    public Date getExpirationDate(String token) {
        Claims claims = validateToken(token);
        return claims.getExpiration();
    }
    
    /**
     * Check if token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDate(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Check if token is valid.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
