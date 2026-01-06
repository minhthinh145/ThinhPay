package com.thinhpay.backend.modules.corebanking.application.dto.res;

import com.thinhpay.backend.modules.corebanking.domain.account.Account;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponse {
    UUID accountId;
    String currency;
    BigDecimal balance;
    String status;

    /**
     * Create an AccountResponse representing the given Account.
     *
     * Maps the source account's id -> accountId, currency code -> currency, balance -> balance,
     * and status name -> status.
     *
     * @param account the domain Account to convert
     * @return an AccountResponse with `accountId`, `currency` (ISO code), `balance`, and `status` populated from the source account
     */
    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .accountId(account.getId())
                .currency(account.getCurrency().getCode())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .build();
    }
}