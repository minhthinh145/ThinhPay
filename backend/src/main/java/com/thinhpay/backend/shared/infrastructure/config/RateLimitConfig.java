package com.thinhpay.backend.shared.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Rate Limiting Configuration
 *
 * Defines rate limits for different endpoints to prevent abuse.
 * Configured via application.yaml under "rate-limit" prefix.
 */
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
@Data
@Slf4j
public class RateLimitConfig {

    /**
     * Rate limits for specific endpoints as a list.
     */
    private List<EndpointLimitWithPath> limits = new ArrayList<>();

    /**
     * Default rate limit for unspecified endpoints.
     */
    private EndpointLimit defaultLimit = new EndpointLimit(100, 100, "1m");

    /**
     * Rate limit configuration with path for list-based config.
     */
    @Data
    public static class EndpointLimitWithPath extends EndpointLimit {
        private String path;

        public EndpointLimitWithPath() {
            super();
        }
    }

    /**
     * Rate limit configuration for a specific endpoint.
     */
    @Data
    public static class EndpointLimit {
        /**
         * Maximum capacity of the bucket (burst size)
         */
        private int capacity;

        /**
         * Number of tokens to refill per interval
         */
        private int tokens;

        /**
         * Refill interval (e.g., "1m", "15m", "1h", "1d")
         * Format: number + unit (s=seconds, m=minutes, h=hours, d=days)
         */
        private String interval;

        public EndpointLimit() {
        }

        public EndpointLimit(int capacity, int tokens, String interval) {
            this.capacity = capacity;
            this.tokens = tokens;
            this.interval = interval;
        }

        /**
         * Parse interval string to milliseconds.
         * Examples: "1m" = 60000ms, "15m" = 900000ms, "1h" = 3600000ms
         */
        public long getIntervalMillis() {
            if (interval == null || interval.isEmpty()) {
                return 60000; // default 1 minute
            }

            char unit = interval.charAt(interval.length() - 1);
            int value = Integer.parseInt(interval.substring(0, interval.length() - 1));

            return switch (unit) {
                case 's' -> value * 1000L;           // seconds
                case 'm' -> value * 60 * 1000L;      // minutes
                case 'h' -> value * 60 * 60 * 1000L; // hours
                case 'd' -> value * 24 * 60 * 60 * 1000L; // days
                default -> 60000L; // default 1 minute
            };
        }
    }

    /**
     * Get rate limit configuration for a specific endpoint.
     * Returns default limit if endpoint not configured.
     */
    public EndpointLimit getLimitForEndpoint(String path) {
        for (EndpointLimitWithPath limit : limits) {
            if (path.equals(limit.getPath())) {
                log.debug("✅ Found endpoint config for '{}': {} requests per {}",
                    path, limit.getCapacity(), limit.getInterval());
                return limit;
            }
        }

        log.warn("⚠️  No endpoint config for '{}', using default: {} requests per {}",
            path, defaultLimit.getCapacity(), defaultLimit.getInterval());
        log.warn("   Available endpoints: {}",
            limits.stream().map(EndpointLimitWithPath::getPath).toList());
        return defaultLimit;
    }

    /**
     * Log rate limit configuration on startup
     */
    @PostConstruct
    public void logConfiguration() {
        log.info("🔒 Rate Limiting Configuration Loaded:");
        log.info("  Default Limit: {} requests per {}", defaultLimit.getCapacity(), defaultLimit.getInterval());

        if (!limits.isEmpty()) {
            log.info("  Endpoint-specific limits:");
            limits.forEach(limit ->
                log.info("    {} → {} requests per {}",
                    limit.getPath(), limit.getCapacity(), limit.getInterval())
            );
        } else {
            log.warn("  No endpoint-specific limits configured - using default for all endpoints");
        }
    }
}
