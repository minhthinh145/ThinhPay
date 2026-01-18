package com.thinhpay.backend.modules.iam.domain.user;


public enum UserAccountStatus {
    /**
     * Chờ xác thực email/phone (trạng thái ban đầu)
     */
    PENDING_VERIFICATION,

    /**
     * Hoạt động bình thường (đã verify email + phone)
     */
    ACTIVE,

    /**
     * Tạm ngưng (admin action, có thể mở lại)
     */
    SUSPENDED,

    /**
     * Khóa (security concern, suspicious activity)
     */
    LOCKED,

    /**
     * Đóng vĩnh viễn (không thể mở lại)
     */
    CLOSED
}
