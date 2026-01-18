package com.thinhpay.backend.shared.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing Configuration.
 * Enable JPA auditing for @CreatedDate, @LastModifiedDate, etc.
 *
 * Can be disabled in tests with: spring.jpa.auditing.enabled=false
 */
@Configuration
@EnableJpaAuditing
@ConditionalOnProperty(
    name = "spring.jpa.auditing.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class JpaAuditingConfig {
    // JPA Auditing configuration
}
