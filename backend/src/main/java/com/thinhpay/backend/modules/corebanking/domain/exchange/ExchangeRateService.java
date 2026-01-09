package com.thinhpay.backend.modules.corebanking.domain.exchange;

import java.math.BigDecimal;

public interface ExchangeRateService {
    ExchangeRate getRate(String fromCurrency, String toCurrency);
    BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency);
}
