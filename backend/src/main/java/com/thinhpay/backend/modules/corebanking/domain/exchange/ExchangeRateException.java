package com.thinhpay.backend.modules.corebanking.domain.exchange;

import com.thinhpay.backend.shared.exception.DomainException;

public class ExchangeRateException extends DomainException {

    // Error codes
    public static final String RATE_NOT_FOUND = "EXCHANGE_RATE_NOT_FOUND";
    public static final String API_ERROR = "EXCHANGE_RATE_API_ERROR";
    public static final String CURRENCY_NOT_SUPPORTED = "CURRENCY_NOT_SUPPORTED";
    public static final String TIMEOUT = "EXCHANGE_RATE_TIMEOUT";

    private static final String DEFAULT_ERROR_CODE = "EXCHANGE_RATE_ERROR";

    public ExchangeRateException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public ExchangeRateException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }

    public ExchangeRateException(String message, String errorCode) {
        super(message, errorCode);
    }

    public ExchangeRateException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

    // Factory methods cho specific errors
    public static ExchangeRateException currencyNotSupported(String currency) {
        return new ExchangeRateException(
                "Currency not supported: " + currency,
                CURRENCY_NOT_SUPPORTED
        );
    }

    public static ExchangeRateException apiError(String details, Throwable cause) {
        return new ExchangeRateException(
                "Exchange rate API error: " + details,
                API_ERROR,
                cause
        );
    }

    public static ExchangeRateException timeout() {
        return new ExchangeRateException(
                "Exchange rate API timeout",
                TIMEOUT
        );
    }
}
