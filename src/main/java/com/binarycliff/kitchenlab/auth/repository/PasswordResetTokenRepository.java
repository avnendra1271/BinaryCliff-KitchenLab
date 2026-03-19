package com.binarycliff.kitchenlab.auth.repository;

import com.binarycliff.kitchenlab.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PasswordResetToken entity operations.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByAdminIdAndIsUsed(UUID adminId, Boolean isUsed);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now OR t.isUsed = true")
    void cleanupExpiredOrUsedTokens(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.adminId = :adminId AND t.isUsed = false AND t.expiresAt > :now")
    Long countValidTokensByAdminId(@Param("adminId") UUID adminId, @Param("now") LocalDateTime now);
}
