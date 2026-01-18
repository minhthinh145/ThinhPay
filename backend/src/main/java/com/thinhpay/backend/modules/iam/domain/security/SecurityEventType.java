package com.thinhpay.backend.modules.iam.domain.security;

/**
 * Security event types - các loại sự kiện bảo mật cần log.
 */
public enum SecurityEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    LOGOUT_ALL,
    PASSWORD_CHANGED,
    PASSWORD_RESET_REQUESTED,
    OTP_GENERATED,
    OTP_VERIFIED,
    OTP_FAILED,
    EMAIL_VERIFIED,
    PHONE_VERIFIED,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    ACCOUNT_SUSPENDED,
    SUSPICIOUS_ACTIVITY,
    TOKEN_REFRESHED,
    TOKEN_BLACKLISTED
}
