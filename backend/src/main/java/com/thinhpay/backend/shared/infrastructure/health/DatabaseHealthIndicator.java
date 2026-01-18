package com.thinhpay.backend.shared.infrastructure.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Custom Health Indicator for Database.
 *
 * Checks if PostgreSQL database is up and responsive.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            // Try to execute a simple query
            boolean valid = connection.isValid(2); // 2 second timeout

            if (valid) {
                String dbProduct = connection.getMetaData().getDatabaseProductName();
                String dbVersion = connection.getMetaData().getDatabaseProductVersion();

                return Health.up()
                    .withDetail("database", dbProduct)
                    .withDetail("version", dbVersion)
                    .withDetail("status", "Connected")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Connection not valid")
                    .build();
            }

        } catch (Exception e) {
            log.error("Database health check failed", e);
            return Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("status", "Disconnected")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
