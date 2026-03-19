package com.binarycliff.kitchenlab.auth.service;

import com.binarycliff.kitchenlab.auth.entity.UserSession;
import com.binarycliff.kitchenlab.auth.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing user sessions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {
    
    private final UserSessionRepository sessionRepository;
    
    /**
     * Create a new user session.
     */
    public UserSession createSession(UUID adminId, String sessionId, String accessToken, 
                                   String refreshToken, String ipAddress, String userAgent, 
                                   LocalDateTime expiresAt) {
        UserSession session = UserSession.builder()
                .id(sessionId)
                .adminId(adminId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .isActive(true)
                .expiresAt(expiresAt)
                .lastAccessedAt(LocalDateTime.now())
                .build();
        
        session = sessionRepository.save(session);
        log.info("Created new session for admin: {}", adminId);
        return session;
    }
    
    /**
     * Get active sessions for a user.
     */
    @Transactional(readOnly = true)
    public List<UserSession> getActiveSessions(UUID adminId) {
        return sessionRepository.findByAdminIdAndIsActive(adminId, true);
    }
    
    /**
     * Get all sessions for a user.
     */
    @Transactional(readOnly = true)
    public List<UserSession> getAllSessions(UUID adminId) {
        return sessionRepository.findByAdminId(adminId);
    }
    
    /**
     * Find session by access token.
     */
    @Transactional(readOnly = true)
    public Optional<UserSession> findByAccessToken(String accessToken) {
        return sessionRepository.findByAccessToken(accessToken);
    }
    
    /**
     * Find session by refresh token.
     */
    @Transactional(readOnly = true)
    public Optional<UserSession> findByRefreshToken(String refreshToken) {
        return sessionRepository.findByRefreshToken(refreshToken);
    }
    
    /**
     * Update last accessed time for session.
     */
    public void updateLastAccessed(String sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setLastAccessedAt(LocalDateTime.now());
            sessionRepository.save(session);
        });
    }
    
    /**
     * Deactivate a specific session.
     */
    public void deactivateSession(String sessionId) {
        sessionRepository.deactivateSessionById(sessionId);
        log.info("Deactivated session: {}", sessionId);
    }
    
    /**
     * Deactivate session by access token.
     */
    public void deactivateSessionByAccessToken(String accessToken) {
        sessionRepository.deactivateSessionByAccessToken(accessToken);
        log.info("Deactivated session by access token");
    }
    
    /**
     * Deactivate all sessions for a user.
     */
    public void deactivateAllSessions(UUID adminId) {
        sessionRepository.deactivateAllSessionsByAdminId(adminId);
        log.info("Deactivated all sessions for admin: {}", adminId);
    }
    
    /**
     * Get count of active sessions for a user.
     */
    @Transactional(readOnly = true)
    public long getActiveSessionCount(UUID adminId) {
        return sessionRepository.countActiveSessionsByAdminId(adminId);
    }
    
    /**
     * Cleanup expired sessions.
     */
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusDays(7); // Remove inactive sessions older than 7 days
        sessionRepository.cleanupExpiredSessions(now, cutoff);
        log.info("Cleaned up expired sessions");
    }
    
    /**
     * Get expired active sessions.
     */
    @Transactional(readOnly = true)
    public List<UserSession> getExpiredActiveSessions() {
        return sessionRepository.findExpiredActiveSessions(LocalDateTime.now());
    }
}
