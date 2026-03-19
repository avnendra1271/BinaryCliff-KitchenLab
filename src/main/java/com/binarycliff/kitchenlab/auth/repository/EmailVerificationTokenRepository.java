package com.binarycliff.kitchenlab.auth.repository;

import com.binarycliff.kitchenlab.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EmailVerificationToken entity operations.
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    
    Optional<EmailVerificationToken> findByToken(String token);
    
    Optional<EmailVerificationToken> findByEmailAndIsUsed(String email, Boolean isUsed);
    
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :now OR t.isUsed = true")
    void cleanupExpiredOrUsedTokens(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(t) FROM EmailVerificationToken t WHERE t.email = :email AND t.isUsed = false AND t.expiresAt > :now")
    Long countValidTokensByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
}
