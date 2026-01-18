package com.thinhpay.backend.modules.iam.domain.otp;

import com.thinhpay.backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity quản lý OTP codes cho 2FA và verification.
 * Supports: Email OTP, SMS OTP, Phone Call OTP.
 *
 * Business Rules:
 * - OTP có TTL 5 phút
 * - Tối đa 3 lần verify (sau đó phải generate OTP mới)
 * - Mỗi OTP chỉ verify được 1 lần (one-time use)
 * - OTP phải 6 chữ số (numeric)
 */
@Entity
@Table(name = "iam_otp_codes", indexes = {
        @Index(name = "idx_otp_code_lookup", columnList = "user_id, code"),
        @Index(name = "idx_otp_user_purpose", columnList = "user_id, purpose, verified"),
        @Index(name = "idx_otp_expires", columnList = "expires_at")
})
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString(callSuper = true, exclude = {"code"})
public class IamOtpCode extends BaseEntity {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 3;
    private static final int DEFAULT_TTL_MINUTES = 5;

    @NotNull
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Size(max = 6)
    @NotNull
    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private OtpType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 50)
    private OtpPurpose purpose;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "verified", nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    // ========== Factory Method ========== //

    /**
     * Generate OTP mới cho user.
     *
     * @param userId      ID của user
     * @param type        Kênh gửi OTP (EMAIL, SMS, PHONE_CALL)
     * @param purpose     Mục đích (VERIFY_EMAIL, LOGIN, TRANSFER...)
     * @param ttlMinutes  Time-to-live in minutes (default: 5)
     * @return OTP entity mới
     */
    public static IamOtpCode generate(UUID userId, OtpType type, OtpPurpose purpose, int ttlMinutes) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được null");
        }
        if (type == null) {
            throw new IllegalArgumentException("OTP type không được null");
        }
        if (purpose == null) {
            throw new IllegalArgumentException("OTP purpose không được null");
        }
        if (ttlMinutes <= 0) {
            throw new IllegalArgumentException("TTL phải > 0");
        }

        String code = generateRandomCode();
        Instant now = Instant.now();
        Instant expiryTime = now.plusSeconds(ttlMinutes * 60L);

        return IamOtpCode.builder()
                .userId(userId)
                .code(code)
                .type(type)
                .purpose(purpose)
                .expiresAt(expiryTime)
                .verified(false)
                .attempts(0)
                .build();
    }

    /**
     * Overload: Generate OTP với default TTL = 5 minutes.
     */
    public static IamOtpCode generate(UUID userId, OtpType type, OtpPurpose purpose) {
        return generate(userId, type, purpose, DEFAULT_TTL_MINUTES);
    }

    // ========== Domain Methods ========== //

    /**
     * Verify OTP code.
     *
     * @param inputCode Code user nhập vào
     * @return true nếu verify thành công
     * @throws IllegalStateException nếu OTP expired, đã verified, hoặc quá số lần thử
     */
    public boolean verify(String inputCode) {
        // Validation
        if (this.verified) {
            throw new IllegalStateException("OTP đã được verify rồi");
        }
        if (isExpired()) {
            throw new IllegalStateException("OTP đã hết hạn");
        }
        if (this.attempts >= MAX_ATTEMPTS) {
            throw new IllegalStateException("Đã vượt quá số lần thử (max 3)");
        }

        // Increment attempts
        this.attempts++;

        // Check code match
        if (this.code.equals(inputCode)) {
            this.verified = true;
            this.verifiedAt = Instant.now();
            return true;
        }

        return false;
    }

    /**
     * Check OTP đã hết hạn chưa.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    /**
     * Check OTP đã được verify chưa.
     */
    public boolean isVerified() {
        return this.verified;
    }

    /**
     * Check OTP còn valid không (chưa expired, chưa verified, chưa quá số lần thử).
     */
    public boolean isValid() {
        return !isExpired() && !this.verified && this.attempts < MAX_ATTEMPTS;
    }

    /**
     * Check OTP có thuộc về user này không.
     */
    public boolean belongsToUser(UUID userId) {
        return this.userId.equals(userId);
    }

    /**
     * Get số lần thử còn lại.
     */
    public int getRemainingAttempts() {
        return Math.max(0, MAX_ATTEMPTS - this.attempts);
    }

    /**
     * Generate 6-digit random OTP code.
     */
    private static String generateRandomCode() {
        int code = RANDOM.nextInt(1000000); // 0 to 999999
        return String.format("%06d", code); // Pad with leading zeros
    }
}

