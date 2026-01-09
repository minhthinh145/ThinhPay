package com.thinhpay.backend.modules.corebanking.domain.exchange;

import com.thinhpay.backend.shared.domain.ValueObject;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Value
public class ExchangeRate implements ValueObject {
    String fromCurrency;
    String toCurrency;
    BigDecimal rate;

    public BigDecimal convert(BigDecimal amount) {
        if (fromCurrency.equals(toCurrency)) {
        return amount;
        }
        return amount.multiply(rate).setScale(4, RoundingMode.HALF_UP);
    }

    public static ExchangeRate of(String from, String to, BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }
        return new ExchangeRate(from.toUpperCase(), to.toUpperCase(), rate);
    }
}
