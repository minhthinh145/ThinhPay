package com.thinhpay.backend.modules.iam.domain.otp;

/**
 * Enum định nghĩa các kênh gửi OTP.
 * Match với DB constraint: chk_otp_type
 */
public enum OtpType {
    /**
     * OTP gửi qua email
     */
    EMAIL,

    /**
     * OTP gửi qua SMS
     */
    SMS,

    /**
     * OTP gửi qua cuộc gọi tự động (voice call)
     */
    PHONE_CALL
}

