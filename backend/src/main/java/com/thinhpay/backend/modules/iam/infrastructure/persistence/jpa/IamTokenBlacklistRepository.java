package com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.iam.domain.token.BlacklistReason;
import com.thinhpay.backend.modules.iam.domain.token.IamTokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho IamTokenBlacklist entity.
 * Provides fast lookup for blacklisted tokens (critical for every API request).
 */
@Repository
public interface IamTokenBlacklistRepository extends JpaRepository<IamTokenBlacklist, UUID> {

    /**
     * Check xem JTI có bị blacklist không.
     * CRITICAL: Query này chạy trên mỗi authenticated request.
     * Index: idx_blacklist_jti (UNIQUE) đảm bảo O(1) lookup.
     */
    boolean existsByJti(String jti);

    /**
     * Tìm blacklist entry theo JTI.
     * Dùng khi cần thêm thông tin (reason, user_id...).
     */
    Optional<IamTokenBlacklist> findByJti(String jti);

    /**
     * Cleanup: Xóa các blacklist entries đã expired.
     * Tokens đã hết hạn tự nhiên không cần blacklist nữa.
     * Chạy scheduled job mỗi giờ.
     */
    @Modifying
    @Query("DELETE FROM IamTokenBlacklist t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Đếm số lượng tokens bị blacklist của user.
     * Dùng cho analytics/monitoring.
     */
    long countByUserId(UUID userId);

    /**
     * Lấy blacklist entries của user theo reason.
     * Dùng cho audit/investigation.
     */
    long countByUserIdAndReason(UUID userId, BlacklistReason reason);
}

