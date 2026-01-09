package com.thinhpay.backend.modules.corebanking.infrastructure.external;

import com.thinhpay.backend.modules.corebanking.domain.exchange.ExchangeRate;
import com.thinhpay.backend.modules.corebanking.domain.exchange.ExchangeRateException;
import com.thinhpay.backend.modules.corebanking.domain.exchange.ExchangeRateService;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExchangeRateApiClient implements ExchangeRateService {
    static final String API_URL = "https://api.exchangerate-api.com/v4/latest/";
    final WebClient webClient;

    public ExchangeRateApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(API_URL).build();
    }

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        return getRate(fromCurrency, toCurrency).convert(amount);
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "#fromCurrency + '_' + #toCurrency", unless = "#result == null")
    public ExchangeRate getRate(String fromCurrency, String toCurrency) {
        log.info("Getting exchange rate from {} to {}", fromCurrency, toCurrency);

        try{
            ExchangeRateResponse response = webClient.get()
                    .uri(fromCurrency)
                    .retrieve()
                    .onStatus(
                            status -> !status.is2xxSuccessful(),
                            clientResponse -> Mono.error(new ExchangeRateException(
                                    "Exchange rate API error: " + clientResponse.statusCode()
                            ))
                    )
                    .bodyToMono(ExchangeRateResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(1)))
                    .block();
            if (response == null || response.getRates() == null) {
                throw ExchangeRateException.apiError("Invalid response from API", null);
            }

            BigDecimal rate = response.getRates().get(toCurrency);

            if(rate == null){
                throw ExchangeRateException.currencyNotSupported(toCurrency);
            }

            log.info("Exchange rate retrieved: 1 {} = {} {}", fromCurrency, rate, toCurrency);
            return ExchangeRate.of(fromCurrency, toCurrency, rate);
        } catch(ExchangeRateException e) {
            throw e;
        } catch(Exception e){
            log.error("Failed to get exchange rate from {} to {}", fromCurrency, toCurrency, e);
            throw ExchangeRateException.apiError(e.getMessage(), e);
        }
    }

    @Data
    private static class ExchangeRateResponse {
        private String base;
        private Map<String, BigDecimal> rates;
    }
}
