package com.thinhpay.backend.modules.corebanking.domain.event;

import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
public class BalanceChangedEvent {
    UUID accountId;
    UUID userId;
    BigDecimal oldBalance;
    BigDecimal newBalance;
    String currency;
    String reason;
    Instant occurredAt;

    public static BalanceChangedEvent of(UUID accountId, UUID userId,
                                         BigDecimal oldBalance, BigDecimal newBalance,
                                         String currency, String reason) {
        return new BalanceChangedEvent(
                accountId, userId, oldBalance, newBalance,
                currency, reason, Instant.now()
        );
    }
}
