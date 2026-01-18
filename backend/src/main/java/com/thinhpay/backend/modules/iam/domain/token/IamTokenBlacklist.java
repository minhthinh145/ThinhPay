package com.thinhpay.backend.modules.iam.domain.token;

import com.thinhpay.backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity quản lý danh sách đen của JWT tokens.
 * Dùng để revoke/blacklist tokens khi: logout, password changed, security breach.
 *
 * Business Rules:
 * - Token bị blacklist không thể sử dụng nữa (dù chưa expired)
 * - Blacklist entry tự động xóa sau khi token hết hạn (cleanup job)
 * - Support cả ACCESS token (15 min) và REFRESH token (7 days)
 */
@Entity
@Table(name = "iam_token_blacklist", indexes = {
        @Index(name = "idx_blacklist_jti", columnList = "jti"),
        @Index(name = "idx_blacklist_user", columnList = "user_id"),
        @Index(name = "idx_blacklist_expires", columnList = "expires_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "iam_token_blacklist_jti_key", columnNames = {"jti"})
})
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString(callSuper = true, exclude = {"jti"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IamTokenBlacklist extends BaseEntity {

    @Size(max = 255)
    @NotNull
    @Column(name = "jti", nullable = false, unique = true)
    String jti;

    @NotNull
    @Column(name = "user_id", nullable = false, updatable = false)
    UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 20)
    TokenType tokenType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    BlacklistReason reason;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    // ========== Factory Methods ========== //

    /**
     * Blacklist token khi user logout.
     */
    public static IamTokenBlacklist createForLogout(String jti, UUID userId, TokenType tokenType, Instant expiresAt) {
        validateInputs(jti, userId, tokenType, expiresAt);
        return IamTokenBlacklist.builder()
                .jti(jti)
                .userId(userId)
                .tokenType(tokenType)
                .reason(BlacklistReason.USER_LOGOUT)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Blacklist token khi user đổi password.
     * Tất cả tokens cũ phải bị invalidate để bắt buộc re-login.
     */
    public static IamTokenBlacklist createForPasswordChange(String jti, UUID userId, TokenType tokenType, Instant expiresAt) {
        validateInputs(jti, userId, tokenType, expiresAt);
        return IamTokenBlacklist.builder()
                .jti(jti)
                .userId(userId)
                .tokenType(tokenType)
                .reason(BlacklistReason.PASSWORD_CHANGED)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Blacklist token khi phát hiện security breach hoặc suspicious activity.
     */
    public static IamTokenBlacklist createForSecurityBreach(String jti, UUID userId, TokenType tokenType, Instant expiresAt) {
        validateInputs(jti, userId, tokenType, expiresAt);
        return IamTokenBlacklist.builder()
                .jti(jti)
                .userId(userId)
                .tokenType(tokenType)
                .reason(BlacklistReason.SECURITY_BREACH)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Blacklist token khi admin revoke manually.
     */
    public static IamTokenBlacklist createForAdminRevoke(String jti, UUID userId, TokenType tokenType, Instant expiresAt) {
        validateInputs(jti, userId, tokenType, expiresAt);
        return IamTokenBlacklist.builder()
                .jti(jti)
                .userId(userId)
                .tokenType(tokenType)
                .reason(BlacklistReason.ADMIN_REVOKE)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Blacklist token khi phát hiện suspicious activity.
     */
    public static IamTokenBlacklist createForSuspiciousActivity(String jti, UUID userId, TokenType tokenType, Instant expiresAt) {
        validateInputs(jti, userId, tokenType, expiresAt);
        return IamTokenBlacklist.builder()
                .jti(jti)
                .userId(userId)
                .tokenType(tokenType)
                .reason(BlacklistReason.SUSPICIOUS_ACTIVITY)
                .expiresAt(expiresAt)
                .build();
    }

    private static void validateInputs(String jti, UUID userId, TokenType tokenType, Instant expiresAt) {
        if (jti == null || jti.isBlank()) {
            throw new IllegalArgumentException("JTI không được null hoặc empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được null");
        }
        if (tokenType == null) {
            throw new IllegalArgumentException("Token type không được null");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("Expires at không được null");
        }
    }

    // ========== Domain Methods ========== //

    /**
     * Check xem token đã hết hạn chưa.
     * Expired tokens sẽ được cleanup job xóa khỏi DB.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    /**
     * Check xem token có thuộc về user này không.
     */
    public boolean belongsToUser(UUID userId) {
        return this.userId.equals(userId);
    }
}