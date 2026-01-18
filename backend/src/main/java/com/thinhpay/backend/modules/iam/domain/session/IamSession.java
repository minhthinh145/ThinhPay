package com.thinhpay.backend.modules.iam.domain.session;

import com.thinhpay.backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity quản lý phiên đăng nhập của người dùng.
 * Supports: Multi-device login, session tracking, auto-logout inactive users.
 *
 * Business Rules:
 * - Session expires after 30 minutes of inactivity (configurable)
 * - User can have multiple active sessions (different devices)
 * - Logout invalidates session immediately
 * - Expired sessions are soft-deleted (active = false)
 */
@Entity
@Table(name = "iam_sessions", indexes = {
        @Index(name = "idx_sessions_token", columnList = "session_token", unique = true),
        @Index(name = "idx_sessions_user_active", columnList = "user_id, active"),
        @Index(name = "idx_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_sessions_expires", columnList = "expires_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "iam_sessions_session_token_key", columnNames = {"session_token"})
})
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString(callSuper = true, exclude = {"sessionToken"})
public class IamSession extends BaseEntity {

    @NotNull
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @NotNull
    @Size(max = 500)
    @Column(name = "session_token", nullable = false, unique = true, length = 500)
    private String sessionToken;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Size(max = 255)
    @Column(name = "device_id", length = 255)
    private String deviceId;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @NotNull
    @Column(name = "last_activity_at", nullable = false)
    @Builder.Default
    private Instant lastActivityAt = Instant.now();

    // ========== Factory Method ========== //

    /**
     * Tạo session mới khi user đăng nhập thành công.
     *
     * @param userId        ID của user
     * @param sessionToken  Unique session token (UUID hoặc random string)
     * @param ipAddress     IP address của client (IPv4/IPv6)
     * @param userAgent     Browser/App user agent string
     * @param deviceId      Device fingerprint hoặc UUID
     * @param ttlMinutes    Time-to-live in minutes (default: 30)
     * @return Session entity mới
     */
    public static IamSession create(
            UUID userId,
            String sessionToken,
            String ipAddress,
            String userAgent,
            String deviceId,
            int ttlMinutes
    ) {
        // Validation
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được null");
        }
        if (sessionToken == null || sessionToken.isBlank()) {
            throw new IllegalArgumentException("Session token không được trống");
        }
        if (ttlMinutes <= 0) {
            throw new IllegalArgumentException("TTL phải > 0");
        }

        Instant now = Instant.now();
        Instant expiryTime = now.plusSeconds(ttlMinutes * 60L);

        return IamSession.builder()
                .userId(userId)
                .sessionToken(sessionToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceId(deviceId)
                .expiresAt(expiryTime)
                .active(true)
                .lastActivityAt(now)
                .build();
    }

    // ========== Domain Methods ========== //

    /**
     * Update last activity time và extend session expiry.
     * Gọi method này mỗi khi user thực hiện request authenticated.
     *
     * @param additionalMinutes Số phút để extend thêm (default: 30)
     */
    public void updateActivity(int additionalMinutes) {
        if (!this.active) {
            throw new IllegalStateException("Không thể update inactive session");
        }
        if (isExpired()) {
            throw new IllegalStateException("Không thể update expired session");
        }

        Instant now = Instant.now();
        this.lastActivityAt = now;
        this.expiresAt = now.plusSeconds(additionalMinutes * 60L);
    }

    /**
     * Overload method: Update activity với default 30 minutes.
     */
    public void updateActivity() {
        updateActivity(30);
    }

    /**
     * Invalidate session (logout).
     * Set active = false để user không thể dùng session này nữa.
     */
    public void invalidate() {
        this.active = false;
    }

    /**
     * Check xem session đã hết hạn chưa.
     *
     * @return true nếu expiresAt < now
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    /**
     * Check session có đang active và chưa expired không.
     *
     * @return true nếu active == true && !isExpired()
     */
    public boolean isActive() {
        return this.active && !isExpired();
    }

    /**
     * Extend session expiry time thêm X phút.
     * Dùng cho "Remember me" functionality.
     *
     * @param additionalMinutes Số phút extend thêm
     */
    public void extendSession(int additionalMinutes) {
        if (!this.active) {
            throw new IllegalStateException("Không thể extend inactive session");
        }
        if (additionalMinutes <= 0) {
            throw new IllegalArgumentException("Additional minutes phải > 0");
        }

        this.expiresAt = this.expiresAt.plusSeconds(additionalMinutes * 60L);
    }

    /**
     * Check xem session sắp hết hạn không (trong vòng 5 phút nữa).
     * Dùng để warning user hoặc auto-refresh.
     *
     * @return true nếu còn < 5 phút
     */
    public boolean isAboutToExpire() {
        Instant fiveMinutesLater = Instant.now().plusSeconds(5 * 60L);
        return this.expiresAt.isBefore(fiveMinutesLater);
    }

    /**
     * Get số phút còn lại trước khi session hết hạn.
     *
     * @return Minutes remaining, hoặc 0 nếu đã expired
     */
    public long getMinutesRemaining() {
        if (isExpired()) {
            return 0;
        }
        long secondsRemaining = this.expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        return secondsRemaining / 60;
    }
}