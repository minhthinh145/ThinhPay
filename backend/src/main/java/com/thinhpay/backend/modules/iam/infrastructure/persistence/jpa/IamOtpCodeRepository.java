package com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.iam.domain.otp.IamOtpCode;
import com.thinhpay.backend.modules.iam.domain.otp.OtpPurpose;
import com.thinhpay.backend.modules.iam.domain.otp.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho IamOtpCode entity.
 * Provides queries for OTP generation, verification, and cleanup.
 */
@Repository
public interface IamOtpCodeRepository extends JpaRepository<IamOtpCode, UUID> {

    /**
     * Tìm OTP code theo user, code và purpose.
     * Dùng khi verify OTP.
     */
    Optional<IamOtpCode> findByUserIdAndCodeAndPurpose(UUID userId, String code, OtpPurpose purpose);

    /**
     * Tìm latest OTP chưa verify của user theo purpose.
     * Dùng để kiểm tra OTP còn valid không (tránh spam generate OTP).
     */
    Optional<IamOtpCode> findFirstByUserIdAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
            UUID userId,
            OtpPurpose purpose
    );

    /**
     * Lấy tất cả OTP chưa verify của user theo purpose.
     * Dùng để invalidate OTP cũ khi generate OTP mới.
     */
    List<IamOtpCode> findByUserIdAndPurposeAndVerifiedFalse(UUID userId, OtpPurpose purpose);

    /**
     * Đếm số lượng OTP đã generate trong khoảng thời gian (rate limiting).
     * Tránh spam generate OTP.
     */
    @Query("SELECT COUNT(o) FROM IamOtpCode o WHERE o.userId = :userId AND o.purpose = :purpose AND o.createdAt >= :since")
    long countRecentOtpCodes(
            @Param("userId") UUID userId,
            @Param("purpose") OtpPurpose purpose,
            @Param("since") Instant since
    );

    /**
     * Cleanup: Xóa OTP đã expired hoặc đã verified.
     * Chạy scheduled job mỗi giờ.
     */
    @Modifying
    @Query("DELETE FROM IamOtpCode o WHERE o.expiresAt < :now OR o.verified = true")
    void deleteExpiredAndVerifiedOtpCodes(@Param("now") Instant now);

    /**
     * Tìm OTP theo user và type (EMAIL, SMS, PHONE_CALL).
     * Dùng cho analytics/monitoring.
     */
    List<IamOtpCode> findByUserIdAndType(UUID userId, OtpType type);

    /**
     * Check xem có OTP nào đang valid cho user + purpose không.
     * Dùng để tránh generate OTP mới khi OTP cũ còn valid.
     */
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM IamOtpCode o " +
           "WHERE o.userId = :userId AND o.purpose = :purpose AND o.verified = false " +
           "AND o.expiresAt > :now AND o.attempts < 3")
    boolean hasValidOtp(@Param("userId") UUID userId, @Param("purpose") OtpPurpose purpose, @Param("now") Instant now);

    /**
     * Xóa tất cả OTP đã expired trước thời điểm chỉ định.
     * Dùng cho scheduled cleanup job.
     */
    @Modifying
    void deleteByExpiresAtBefore(Instant now);
}

