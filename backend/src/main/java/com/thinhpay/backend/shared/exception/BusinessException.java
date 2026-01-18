package com.thinhpay.backend.shared.exception;

/**
 * Exception cho các business rule violations (số dư không đủ, vượt hạn mức...).
 */
public class BusinessException extends DomainException {

    public BusinessException(String message) {
        super(message, "BUS_001");
    }

    public BusinessException(String message, String errorCode) {
        super(message, errorCode);
    }

    public static BusinessException insufficientBalance() {
        return new BusinessException("Số dư không đủ để thực hiện giao dịch", "BUS_002");
    }

    public static BusinessException transactionLimitExceeded() {
        return new BusinessException("Vượt quá hạn mức giao dịch", "BUS_003");
    }

    public static BusinessException dailyLimitExceeded() {
        return new BusinessException("Vượt quá hạn mức giao dịch trong ngày", "BUS_004");
    }

    public static BusinessException accountNotActive() {
        return new BusinessException("Tài khoản chưa được kích hoạt", "BUS_005");
    }

    public static BusinessException sessionExpired() {
        return new BusinessException("Phiên đăng nhập đã hết hạn", "BUS_006");
    }
}
