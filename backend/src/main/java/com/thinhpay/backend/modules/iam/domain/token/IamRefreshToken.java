package com.thinhpay.backend.modules.iam.domain.token;

import com.thinhpay.backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity quản lý Refresh Tokens cho JWT authentication.
 * Dùng để renew Access Token mà không cần re-login.
 *
 * Business Rules:
 * - Refresh token có TTL dài hơn Access token (7 days vs 15 minutes)
 * - User có thể có nhiều refresh tokens (multi-device login)
 * - Refresh token chỉ dùng 1 lần (rotation pattern)
 * - Revoked tokens không thể dùng lại
 */
@Entity
@Table(name = "iam_refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_token", columnList = "token"),
        @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
        @Index(name = "idx_refresh_tokens_expires", columnList = "expires_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "iam_refresh_tokens_token_key", columnNames = {"token"})
})
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString(callSuper = true, exclude = {"token"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IamRefreshToken extends BaseEntity {

    @NotNull
    @Column(name = "user_id", nullable = false, updatable = false)
    UUID userId;

    @Size(max = 500)
    @NotNull
    @Column(name = "token", nullable = false, unique = true, length = 500)
    String token;

    @Size(max = 255)
    @Column(name = "device_fingerprint", length = 255)
    String deviceFingerprint;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "revoked", nullable = false)
    @Builder.Default
    Boolean revoked = false;

    @NotNull
    @Column(name = "last_used_at", nullable = false)
    @Builder.Default
    Instant lastUsedAt = Instant.now();

    // ========== Factory Method ========== //

    /**
     * Tạo refresh token mới khi user login hoặc refresh token rotation.
     *
     * @param userId            ID của user
     * @param token             Unique refresh token (UUID hoặc random string)
     * @param deviceFingerprint Device fingerprint để detect device switching
     * @param ttlDays           Time-to-live in days (default: 7)
     * @return RefreshToken entity mới
     */
    public static IamRefreshToken create(UUID userId, String token, String deviceFingerprint, int ttlDays) {
        // Validation
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được null");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token không được trống");
        }
        if (ttlDays <= 0) {
            throw new IllegalArgumentException("TTL phải > 0");
        }

        Instant now = Instant.now();
        Instant expiryTime = now.plusSeconds(ttlDays * 24L * 60L * 60L);

        return IamRefreshToken.builder()
                .userId(userId)
                .token(token)
                .deviceFingerprint(deviceFingerprint)
                .expiresAt(expiryTime)
                .revoked(false)
                .lastUsedAt(now)
                .build();
    }

    // ========== Domain Methods ========== //

    /**
     * Revoke refresh token (logout hoặc token rotation).
     * Một khi revoked thì không thể dùng lại.
     */
    public void revoke() {
        if (this.revoked) {
            throw new IllegalStateException("Token đã bị revoke rồi");
        }
        this.revoked = true;
    }

    /**
     * Update last used timestamp.
     * Gọi mỗi khi token được dùng để refresh access token.
     */
    public void updateLastUsed() {
        if (this.revoked) {
            throw new IllegalStateException("Không thể update revoked token");
        }
        if (isExpired()) {
            throw new IllegalStateException("Không thể update expired token");
        }
        this.lastUsedAt = Instant.now();
    }

    /**
     * Check xem token đã hết hạn chưa.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    /**
     * Check xem token đã bị revoke chưa.
     */
    public boolean isRevoked() {
        return this.revoked;
    }

    /**
     * Check token có valid không (chưa expired và chưa revoked).
     */
    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }

    /**
     * Check token có thuộc về user này không.
     */
    public boolean belongsToUser(UUID userId) {
        return this.userId.equals(userId);
    }

    /**
     * Check xem token có đang được dùng từ cùng device không.
     * Dùng để detect device switching (potential security issue).
     */
    public boolean matchesDevice(String deviceFingerprint) {
        if (this.deviceFingerprint == null || deviceFingerprint == null) {
            return true; // Skip check nếu không có fingerprint
        }
        return this.deviceFingerprint.equals(deviceFingerprint);
    }
}

