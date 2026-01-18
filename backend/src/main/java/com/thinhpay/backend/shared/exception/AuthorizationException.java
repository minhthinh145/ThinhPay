package com.thinhpay.backend.shared.exception;

/**
 * Exception khi user không có quyền thực hiện action.
 */
public class AuthorizationException extends DomainException {

    public AuthorizationException(String message) {
        super(message, "AUTHZ_001");
    }

    public AuthorizationException(String message, String errorCode) {
        super(message, errorCode);
    }

    public static AuthorizationException insufficientPermissions() {
        return new AuthorizationException("Bạn không có quyền thực hiện thao tác này", "AUTHZ_002");
    }

    public static AuthorizationException accountSuspended() {
        return new AuthorizationException("Tài khoản đã bị tạm ngưng", "AUTHZ_003");
    }

    public static AuthorizationException accountLocked() {
        return new AuthorizationException("Tài khoản đã bị khóa", "AUTHZ_004");
    }

    public static AuthorizationException kycRequired(String kycLevel) {
        return new AuthorizationException(
            String.format("Yêu cầu xác thực KYC level %s để thực hiện giao dịch này", kycLevel),
            "AUTHZ_005"
        );
    }
}
