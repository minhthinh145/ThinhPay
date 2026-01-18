package com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.iam.domain.security.IamSecurityLog;
import com.thinhpay.backend.modules.iam.domain.security.RiskLevel;
import com.thinhpay.backend.modules.iam.domain.security.SecurityEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository cho IamSecurityLog entity.
 */
@Repository
public interface IamSecurityLogRepository extends JpaRepository<IamSecurityLog, UUID> {

    /**
     * Tìm logs của user (paginated).
     */
    Page<IamSecurityLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Tìm logs theo event type.
     */
    List<IamSecurityLog> findByEventTypeOrderByCreatedAtDesc(SecurityEventType eventType);

    /**
     * Tìm logs theo risk level.
     */
    List<IamSecurityLog> findByRiskLevelOrderByCreatedAtDesc(RiskLevel riskLevel);

    /**
     * Tìm logs trong khoảng thời gian.
     */
    @Query("SELECT s FROM IamSecurityLog s WHERE s.createdAt BETWEEN :start AND :end ORDER BY s.createdAt DESC")
    List<IamSecurityLog> findByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Đếm failed login attempts từ IP address trong khoảng thời gian.
     * Dùng để detect brute force attacks.
     */
    @Query("SELECT COUNT(s) FROM IamSecurityLog s WHERE s.eventType = :eventType AND s.ipAddress = :ipAddress AND s.createdAt > :since")
    long countFailedAttemptsByIpSince(
        @Param("eventType") SecurityEventType eventType,
        @Param("ipAddress") String ipAddress,
        @Param("since") Instant since
    );

    /**
     * Tìm high-risk events gần đây (24h).
     */
    @Query("SELECT s FROM IamSecurityLog s WHERE s.riskLevel IN ('HIGH', 'CRITICAL') AND s.createdAt > :since ORDER BY s.createdAt DESC")
    List<IamSecurityLog> findRecentHighRiskEvents(@Param("since") Instant since);
}
