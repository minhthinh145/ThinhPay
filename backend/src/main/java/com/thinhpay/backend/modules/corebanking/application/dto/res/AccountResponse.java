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

    // Factory method for creating AccountResponse
    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .accountId(account.getId())
                .currency(account.getCurrency().getCode())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .build();
    }
}
