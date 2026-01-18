package com.thinhpay.backend.modules.iam.domain.token;

/**
 * Enum định nghĩa các lý do token bị blacklist.
 * Match với DB constraint: chk_blacklist_reason
 */
public enum BlacklistReason {
    /**
     * User tự logout - revoke token thủ công
     */
    USER_LOGOUT,

    /**
     * Admin revoke token (vi phạm chính sách, suspicious user...)
     */
    ADMIN_REVOKE,

    /**
     * User đổi password - tất cả tokens cũ phải invalidate
     */
    PASSWORD_CHANGED,

    /**
     * Phát hiện security breach (account bị hack, leaked credentials...)
     */
    SECURITY_BREACH,

    /**
     * Suspicious activity detected (login từ location lạ, nhiều failed attempts...)
     */
    SUSPICIOUS_ACTIVITY
}

