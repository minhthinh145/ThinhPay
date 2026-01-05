package com.thinhpay.backend.shared.domain;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {
    private final String errorCode;

    // Đảo message lên trước cho đúng chuẩn Java Exception
    protected DomainException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected DomainException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]: %s", this.getClass().getSimpleName(), errorCode, getMessage());
    }
}