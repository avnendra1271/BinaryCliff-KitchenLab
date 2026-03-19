package com.binarycliff.kitchenlab.auth.repository;

import com.binarycliff.kitchenlab.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserSession entity operations.
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    
    List<UserSession> findByAdminId(UUID adminId);
    
    List<UserSession> findByAdminIdAndIsActive(UUID adminId, Boolean isActive);
    
    Optional<UserSession> findByAccessToken(String accessToken);
    
    Optional<UserSession> findByRefreshToken(String refreshToken);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.adminId = :adminId")
    void deactivateAllSessionsByAdminId(@Param("adminId") UUID adminId);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.id = :sessionId")
    void deactivateSessionById(@Param("sessionId") String sessionId);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.accessToken = :accessToken")
    void deactivateSessionByAccessToken(@Param("accessToken") String accessToken);
    
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.adminId = :adminId AND s.isActive = true")
    Long countActiveSessionsByAdminId(@Param("adminId") UUID adminId);
    
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now OR (s.isActive = false AND s.updatedAt < :cutoff)")
    void cleanupExpiredSessions(@Param("now") LocalDateTime now, @Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT s FROM UserSession s WHERE s.isActive = true AND s.expiresAt < :now")
    List<UserSession> findExpiredActiveSessions(@Param("now") LocalDateTime now);
}
