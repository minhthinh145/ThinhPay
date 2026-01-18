package com.thinhpay.backend.shared.exception;

/**
 * Exception khi authentication thất bại (sai password, invalid token...).
 */
public class AuthenticationException extends DomainException {

    public AuthenticationException(String message) {
        super(message, "AUTH_001");
    }

    public AuthenticationException(String message, String errorCode) {
        super(message, errorCode);
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Email hoặc mật khẩu không đúng", "AUTH_002");
    }

    public static AuthenticationException invalidToken() {
        return new AuthenticationException("Token không hợp lệ hoặc đã hết hạn", "AUTH_003");
    }

    public static AuthenticationException tokenExpired() {
        return new AuthenticationException("Token đã hết hạn", "AUTH_004");
    }

    public static AuthenticationException tokenBlacklisted() {
        return new AuthenticationException("Token đã bị thu hồi", "AUTH_005");
    }
}
