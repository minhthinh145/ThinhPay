package com.thinhpay.backend.shared.exception;

import lombok.Getter;

/**
 * Base exception cho tất cả domain exceptions.
 * Chứa errorCode để frontend/client có thể handle specific errors.
 */
@Getter
public abstract class DomainException extends RuntimeException {
    private final String errorCode;

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
