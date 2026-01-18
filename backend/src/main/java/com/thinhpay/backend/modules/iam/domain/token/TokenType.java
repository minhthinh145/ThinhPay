    package com.thinhpay.backend.modules.iam.domain.token;

/**
 * Enum định nghĩa các loại JWT token trong hệ thống.
 *
 * - ACCESS: Short-lived token (15 minutes) dùng để authenticate API requests
 * - REFRESH: Long-lived token (7 days) dùng để renew access token
 */
public enum TokenType {
    /**
     * Access Token - TTL: 15 phút
     * Dùng trong Authorization header: Bearer {access_token}
     */
    ACCESS,

    /**
     * Refresh Token - TTL: 7 ngày
     * Dùng để renew access token khi hết hạn
     */
    REFRESH
}

