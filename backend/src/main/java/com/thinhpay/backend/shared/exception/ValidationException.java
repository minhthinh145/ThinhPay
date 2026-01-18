package com.thinhpay.backend.shared.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception khi validation thất bại (email trùng, OTP sai...).
 */
@Getter
public class ValidationException extends DomainException {
    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message, "VAL_001");
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(String message, String errorCode) {
        super(message, errorCode);
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(String message, String errorCode, Map<String, String> fieldErrors) {
        super(message, errorCode);
        this.fieldErrors = fieldErrors;
    }

    public static ValidationException emailAlreadyExists(String email) {
        return new ValidationException(
            String.format("Email '%s' đã được sử dụng", email),
            "VAL_002"
        );
    }

    public static ValidationException phoneAlreadyExists(String phone) {
        return new ValidationException(
            String.format("Số điện thoại '%s' đã được sử dụng", phone),
            "VAL_003"
        );
    }

    public static ValidationException invalidOtp() {
        return new ValidationException("Mã OTP không đúng", "VAL_004");
    }

    public static ValidationException otpExpired() {
        return new ValidationException("Mã OTP đã hết hạn", "VAL_005");
    }

    public static ValidationException otpMaxAttemptsExceeded() {
        return new ValidationException("Bạn đã nhập sai OTP quá số lần cho phép", "VAL_006");
    }

    public static ValidationException invalidPasswordFormat() {
        return new ValidationException(
            "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số",
            "VAL_007"
        );
    }

    public static ValidationException passwordMismatch() {
        return new ValidationException("Mật khẩu hiện tại không đúng", "VAL_008");
    }
}
