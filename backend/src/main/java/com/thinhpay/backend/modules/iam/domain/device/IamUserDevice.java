package com.thinhpay.backend.modules.iam.domain.device;

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
 * Entity quản lý thiết bị đã đăng nhập của user.
 * Supports: Trusted devices, push notifications, device fingerprinting.
 *
 * Business Rules:
 * - Mỗi device_id là unique (UUID hoặc browser fingerprint)
 * - Trusted device: Bỏ qua OTP verification khi login
 * - FCM token: Gửi push notification cho mobile app
 * - Biometric enabled: Face ID, Touch ID, Fingerprint
 */
@Entity
@Table(name = "iam_user_devices", indexes = {
    @Index(name = "idx_devices_device_id", columnList = "device_id", unique = true),
    @Index(name = "idx_devices_user_id", columnList = "user_id"),
    @Index(name = "idx_devices_user_trusted", columnList = "user_id, trusted")
})
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IamUserDevice extends BaseEntity {

    @NotNull
    @Column(name = "user_id", nullable = false)
    UUID userId;

    @NotNull
    @Size(max = 255)
    @Column(name = "device_id", nullable = false, unique = true, length = 255)
    String deviceId;

    @Size(max = 255)
    @Column(name = "device_name", length = 255)
    String deviceName; // "iPhone 15 Pro", "Samsung S23 Ultra"

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    DeviceType deviceType;

    @Size(max = 50)
    @Column(name = "os_name", length = 50)
    String osName; // "iOS", "Android", "Windows"

    @Size(max = 20)
    @Column(name = "os_version", length = 20)
    String osVersion; // "17.2", "14.0"

    @Size(max = 20)
    @Column(name = "app_version", length = 20)
    String appVersion; // "1.0.0"

    @Size(max = 500)
    @Column(name = "fcm_token", length = 500)
    String fcmToken; // Firebase Cloud Messaging token

    @NotNull
    @Builder.Default
    @Column(name = "trusted", nullable = false)
    Boolean trusted = false;

    @NotNull
    @Builder.Default
    @Column(name = "biometric_enabled", nullable = false)
    Boolean biometricEnabled = false;

    @Column(name = "first_seen_at")
    Instant firstSeenAt;

    @Column(name = "last_seen_at")
    Instant lastSeenAt;

    // ========== Factory Methods ========== //

    /**
     * Tạo device mới khi user login lần đầu từ device này.
     */
    public static IamUserDevice create(UUID userId, String deviceId, String deviceName, DeviceType deviceType) {
        Instant now = Instant.now();
        return IamUserDevice.builder()
            .userId(userId)
            .deviceId(deviceId)
            .deviceName(deviceName)
            .deviceType(deviceType)
            .trusted(false)
            .biometricEnabled(false)
            .firstSeenAt(now)
            .lastSeenAt(now)
            .build();
    }

    // ========== Domain Methods ========== //

    /**
     * Đánh dấu device là trusted (bỏ qua OTP khi login).
     */
    public void markAsTrusted() {
        this.trusted = true;
    }

    /**
     * Bỏ trusted status.
     */
    public void unmarkAsTrusted() {
        this.trusted = false;
    }

    /**
     * Enable biometric authentication.
     */
    public void enableBiometric() {
        this.biometricEnabled = true;
    }

    /**
     * Disable biometric authentication.
     */
    public void disableBiometric() {
        this.biometricEnabled = false;
    }

    /**
     * Update FCM token cho push notifications.
     */
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /**
     * Update last seen timestamp.
     */
    public void updateLastSeen() {
        this.lastSeenAt = Instant.now();
    }

    /**
     * Update device info (name, OS version, app version).
     */
    public void updateDeviceInfo(String deviceName, String osVersion, String appVersion) {
        if (deviceName != null && !deviceName.isBlank()) {
            this.deviceName = deviceName;
        }
        if (osVersion != null && !osVersion.isBlank()) {
            this.osVersion = osVersion;
        }
        if (appVersion != null && !appVersion.isBlank()) {
            this.appVersion = appVersion;
        }
    }
}
