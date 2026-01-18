package com.thinhpay.backend.modules.iam.domain.security;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity ghi lại security events (audit trail).
 * Supports: Login tracking, fraud detection, compliance audit.
 *
 * Business Rules:
 * - Log tất cả critical events: LOGIN, LOGOUT, PASSWORD_CHANGE, OTP_FAILED...
 * - Risk level: LOW, MEDIUM, HIGH, CRITICAL
 * - Metadata chứa additional context (JSON format)
 */
@Entity
@Table(name = "iam_security_logs", indexes = {
    @Index(name = "idx_security_logs_user_id", columnList = "user_id, created_at"),
    @Index(name = "idx_security_logs_event_type", columnList = "event_type, created_at"),
    @Index(name = "idx_security_logs_risk", columnList = "risk_level, created_at"),
    @Index(name = "idx_security_logs_created_at", columnList = "created_at")
})
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IamSecurityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @Column(name = "user_id")
    UUID userId; // Nullable - có thể log anonymous events

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    SecurityEventType eventType;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    String userAgent;

    @Size(max = 255)
    @Column(name = "device_id", length = 255)
    String deviceId;

    @Column(name = "metadata", columnDefinition = "TEXT")
    String metadata; // JSON format - additional context

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "risk_level", nullable = false, length = 20)
    RiskLevel riskLevel = RiskLevel.LOW;

    @NotNull
    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt = Instant.now();

    // ========== Factory Methods ========== //

    /**
     * Log successful login.
     */
    public static IamSecurityLog logLogin(UUID userId, String ipAddress, String userAgent, String deviceId) {
        return IamSecurityLog.builder()
            .userId(userId)
            .eventType(SecurityEventType.LOGIN_SUCCESS)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .deviceId(deviceId)
            .riskLevel(RiskLevel.LOW)
            .build();
    }

    /**
     * Log failed login attempt.
     */
    public static IamSecurityLog logLoginFailed(String email, String ipAddress, String reason) {
        return IamSecurityLog.builder()
            .eventType(SecurityEventType.LOGIN_FAILED)
            .ipAddress(ipAddress)
            .metadata(String.format("{\"email\":\"%s\",\"reason\":\"%s\"}", email, reason))
            .riskLevel(RiskLevel.MEDIUM)
            .build();
    }

    /**
     * Log logout.
     */
    public static IamSecurityLog logLogout(UUID userId, String ipAddress, String deviceId) {
        return IamSecurityLog.builder()
            .userId(userId)
            .eventType(SecurityEventType.LOGOUT)
            .ipAddress(ipAddress)
            .deviceId(deviceId)
            .riskLevel(RiskLevel.LOW)
            .build();
    }

    /**
     * Log password change.
     */
    public static IamSecurityLog logPasswordChange(UUID userId, String ipAddress) {
        return IamSecurityLog.builder()
            .userId(userId)
            .eventType(SecurityEventType.PASSWORD_CHANGED)
            .ipAddress(ipAddress)
            .riskLevel(RiskLevel.MEDIUM)
            .build();
    }

    /**
     * Log OTP failed (brute force detection).
     */
    public static IamSecurityLog logOtpFailed(UUID userId, String ipAddress, int attempts) {
        RiskLevel risk = attempts >= 3 ? RiskLevel.HIGH : RiskLevel.MEDIUM;
        return IamSecurityLog.builder()
            .userId(userId)
            .eventType(SecurityEventType.OTP_FAILED)
            .ipAddress(ipAddress)
            .metadata(String.format("{\"attempts\":%d}", attempts))
            .riskLevel(risk)
            .build();
    }

    /**
     * Log suspicious activity.
     */
    public static IamSecurityLog logSuspiciousActivity(UUID userId, String ipAddress, String description) {
        return IamSecurityLog.builder()
            .userId(userId)
            .eventType(SecurityEventType.SUSPICIOUS_ACTIVITY)
            .ipAddress(ipAddress)
            .metadata(String.format("{\"description\":\"%s\"}", description))
            .riskLevel(RiskLevel.CRITICAL)
            .build();
    }
}
