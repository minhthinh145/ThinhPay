package com.thinhpay.backend.modules.iam.domain.security;

/**
 * Risk level của security event.
 */
public enum RiskLevel {
    LOW,       // Normal activity
    MEDIUM,    // Unusual but not critical
    HIGH,      // Suspicious - requires attention
    CRITICAL   // Confirmed attack - immediate action needed
}
