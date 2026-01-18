package com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.iam.domain.token.IamRefreshToken;
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
 * Repository cho IamRefreshToken entity.
 * Provides queries for token validation, rotation, and cleanup.
 */
@Repository
public interface IamRefreshTokenRepository extends JpaRepository<IamRefreshToken, UUID> {

    /**
     * Tìm refresh token theo token string.
     * Dùng khi validate token hoặc rotation.
     */
    Optional<IamRefreshToken> findByToken(String token);

    /**
     * Lấy tất cả active tokens của user (chưa revoked).
     * Dùng để hiển thị danh sách devices đang login.
     */
    List<IamRefreshToken> findByUserIdAndRevokedFalse(UUID userId);

    /**
     * Revoke tất cả tokens của user.
     * Dùng khi: user đổi password, admin force logout, security breach.
     */
    @Modifying
    @Query("UPDATE IamRefreshToken t SET t.revoked = true WHERE t.userId = :userId AND t.revoked = false")
    void revokeAllUserTokens(@Param("userId") UUID userId);

    /**
     * Cleanup: Xóa tokens đã expired hoặc revoked.
     * Chạy scheduled job mỗi ngày để dọn dẹp DB.
     */
    @Modifying
    @Query("DELETE FROM IamRefreshToken t WHERE t.expiresAt < :now OR t.revoked = true")
    void deleteExpiredAndRevokedTokens(@Param("now") Instant now);

    /**
     * Đếm số lượng active tokens của user.
     * Dùng để limit số lượng devices có thể login cùng lúc.
     */
    long countByUserIdAndRevokedFalse(UUID userId);

    /**
     * Tìm tokens của user theo device fingerprint.
     * Dùng để detect same device re-login.
     */
    Optional<IamRefreshToken> findByUserIdAndDeviceFingerprint(UUID userId, String deviceFingerprint);
}

