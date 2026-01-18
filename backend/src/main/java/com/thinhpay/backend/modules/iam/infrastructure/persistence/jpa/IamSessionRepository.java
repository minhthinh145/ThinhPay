package com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.iam.domain.session.IamSession;
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
 * Repository cho IamSession entity.
 * Provides queries for session management, validation, and cleanup.
 */
@Repository
public interface IamSessionRepository extends JpaRepository<IamSession, UUID> {

    /**
     * Tìm session theo session token.
     * CRITICAL: Query này chạy trên mỗi authenticated request.
     */
    Optional<IamSession> findBySessionToken(String sessionToken);

    /**
     * Lấy tất cả active sessions của user.
     * Dùng để hiển thị danh sách devices đang login.
     */
    List<IamSession> findByUserIdAndActiveTrue(UUID userId);

    /**
     * Lấy tất cả sessions của user (bao gồm inactive).
     * Dùng cho audit/history.
     */
    List<IamSession> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Invalidate tất cả sessions của user.
     * Dùng khi: user đổi password, admin force logout, security breach.
     */
    @Modifying
    @Query("UPDATE IamSession s SET s.active = false WHERE s.userId = :userId AND s.active = true")
    void invalidateAllUserSessions(@Param("userId") UUID userId);

    /**
     * Invalidate session cụ thể theo ID.
     * Dùng khi user logout 1 device cụ thể.
     */
    @Modifying
    @Query("UPDATE IamSession s SET s.active = false WHERE s.id = :sessionId")
    void invalidateSession(@Param("sessionId") UUID sessionId);

    /**
     * Cleanup: Xóa sessions đã expired.
     * Chạy scheduled job mỗi giờ.
     */
    @Modifying
    @Query("DELETE FROM IamSession s WHERE s.expiresAt < :now")
    void deleteExpiredSessions(@Param("now") Instant now);

    /**
     * Cleanup: Xóa inactive sessions cũ (> 30 ngày).
     * Giữ lại history nhưng không quá lâu.
     */
    @Modifying
    @Query("DELETE FROM IamSession s WHERE s.active = false AND s.createdAt < :cutoffDate")
    void deleteOldInactiveSessions(@Param("cutoffDate") Instant cutoffDate);

    /**
     * Đếm số lượng active sessions của user.
     * Dùng để limit số lượng devices có thể login cùng lúc.
     */
    long countByUserIdAndActiveTrue(UUID userId);

    /**
     * Tìm session theo device ID.
     * Dùng để check same device re-login.
     */
    Optional<IamSession> findByUserIdAndDeviceIdAndActiveTrue(UUID userId, String deviceId);

    /**
     * Tìm sessions sắp hết hạn (trong vòng 5 phút).
     * Dùng để gửi warning notification cho user.
     */
    @Query("SELECT s FROM IamSession s WHERE s.active = true AND s.expiresAt BETWEEN :now AND :fiveMinutesLater")
    List<IamSession> findSessionsAboutToExpire(@Param("now") Instant now, @Param("fiveMinutesLater") Instant fiveMinutesLater);
}

