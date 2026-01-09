package com.thinhpay.backend.modules.corebanking.infrastructure;

import com.thinhpay.backend.modules.corebanking.domain.exchange.ExchangeRate;
import com.thinhpay.backend.modules.corebanking.domain.exchange.ExchangeRateException;
import com.thinhpay.backend.modules.corebanking.domain.exchange.ExchangeRateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ExchangeRateApiClientTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("Should get exchange rate successfully")
    void should_GetExchangeRate_Successfully() {
        // When
        ExchangeRate rate = exchangeRateService.getRate("USD", "VND");

        // Then
        assertThat(rate).isNotNull();
        assertThat(rate.getFromCurrency()).isEqualTo("USD");
        assertThat(rate.getToCurrency()).isEqualTo("VND");
        assertThat(rate.getRate()).isGreaterThan(BigDecimal.ZERO);
        assertThat(rate.getRate()).isGreaterThan(new BigDecimal("20000")); // 1 USD > 20,000 VND
    }

    @Test
    @DisplayName("Should convert currency correctly")
    void should_ConvertCurrency_Correctly() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");

        // When
        BigDecimal result = exchangeRateService.convert(amount, "USD", "VND");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(new BigDecimal("2000000")); // 100 USD > 2,000,000 VND
    }

    @Test
    @DisplayName("Should return same amount for same currency")
    void should_ReturnSameAmount_ForSameCurrency() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");

        // When
        BigDecimal result = exchangeRateService.convert(amount, "USD", "USD");

        // Then
        assertThat(result).isEqualByComparingTo(amount);
    }

    @Test
    @DisplayName("Should throw exception for invalid currency")
    void should_ThrowException_ForInvalidCurrency() {
        // When & Then
        assertThatThrownBy(() -> exchangeRateService.getRate("USD", "INVALID"))
                .isInstanceOf(ExchangeRateException.class)
                .hasMessageContaining("Currency not supported");
    }

    @Test
    @DisplayName("Should convert EUR to VND")
    void should_ConvertEurToVnd() {
        // Given
        BigDecimal amount = new BigDecimal("50.00");

        // When
        BigDecimal result = exchangeRateService.convert(amount, "EUR", "VND");

        // Then
        assertThat(result).isGreaterThan(BigDecimal.ZERO);
    }
}
