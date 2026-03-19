package com.binarycliff.kitchenlab.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.UUID;

/**
 * JPA Auditing configuration for automatic audit fields.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    /**
     * Provides the current auditor for audit fields.
     * In a real application, this would return the current user ID.
     * For system operations, we return a default system UUID.
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            // In a real application, get current user from security context
            // For now, return system UUID
            return java.util.Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        };
    }
}
