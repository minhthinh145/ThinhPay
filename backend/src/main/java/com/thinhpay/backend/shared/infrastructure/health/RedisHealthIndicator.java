package com.thinhpay.backend.shared.infrastructure.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Custom Health Indicator for Redis.
 *
 * Checks if Redis is up and responsive.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try {
            // Try to ping Redis
            redisConnectionFactory.getConnection().ping();

            return Health.up()
                .withDetail("service", "Redis")
                .withDetail("status", "Connected")
                .build();

        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return Health.down()
                .withDetail("service", "Redis")
                .withDetail("status", "Disconnected")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
