package com.thinhpay.backend.modules.iam.domain.otp;

/**
 * Enum định nghĩa các mục đích sử dụng OTP.
 * Match với DB constraint: chk_otp_purpose
 */
public enum OtpPurpose {
    /**
     * Xác thực email khi đăng ký mới
     */
    VERIFY_EMAIL,

    /**
     * Xác thực số điện thoại
     */
    VERIFY_PHONE,

    /**
     * Xác thực khi đăng nhập (2FA)
     */
    LOGIN,

    /**
     * Xác thực khi thực hiện giao dịch chuyển tiền
     */
    TRANSFER,

    /**
     * Xác thực khi đổi password
     */
    CHANGE_PASSWORD,

    /**
     * Xác thực khi reset PIN
     */
    RESET_PIN
}

