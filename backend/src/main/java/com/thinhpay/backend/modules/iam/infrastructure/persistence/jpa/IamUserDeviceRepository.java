package com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.iam.domain.device.IamUserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho IamUserDevice entity.
 */
@Repository
public interface IamUserDeviceRepository extends JpaRepository<IamUserDevice, UUID> {

    /**
     * Tìm device theo device_id.
     */
    Optional<IamUserDevice> findByDeviceId(String deviceId);

    /**
     * Tìm tất cả devices của user.
     */
    List<IamUserDevice> findByUserId(UUID userId);

    /**
     * Tìm trusted devices của user.
     */
    List<IamUserDevice> findByUserIdAndTrustedTrue(UUID userId);

    /**
     * Check xem device đã tồn tại chưa.
     */
    boolean existsByDeviceId(String deviceId);

    /**
     * Đếm số devices của user.
     */
    long countByUserId(UUID userId);
}
