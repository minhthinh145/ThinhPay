package com.thinhpay.backend.modules.corebanking.application.service;

import com.thinhpay.backend.modules.corebanking.application.dto.res.AccountResponse;
import com.thinhpay.backend.modules.corebanking.domain.account.Account;
import com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountQueryService {
    AccountRepository accountRepository;

    public List<AccountResponse> getUserAccounts(UUID userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    public Page<AccountResponse> getUserAccountsPaginated(UUID userId, Pageable pageable) {
        return accountRepository.findAllByUserId(userId, pageable)
                .map(AccountResponse::from);
    }

    public AccountResponse getAccountBalance(UUID userId, String currency) {
        return accountRepository.findByUserIdAndCurrency_Code(userId, currency)
                .map(AccountResponse::from)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found for user: " + userId + " with currency: " + currency));
    }
}
