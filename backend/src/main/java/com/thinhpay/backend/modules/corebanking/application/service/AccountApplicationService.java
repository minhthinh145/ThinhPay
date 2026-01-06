package com.thinhpay.backend.modules.corebanking.application.service;

import com.thinhpay.backend.modules.corebanking.application.dto.req.DepositRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.req.WithdrawRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.res.AccountResponse;
import com.thinhpay.backend.modules.corebanking.application.port.in.DepositUseCase;
import com.thinhpay.backend.modules.corebanking.application.port.in.WithdrawUseCase;
import com.thinhpay.backend.modules.corebanking.domain.account.Account;
import com.thinhpay.backend.modules.corebanking.domain.ledger.LedgerEntry;
import com.thinhpay.backend.modules.corebanking.domain.ledger.LedgerEntryType;
import com.thinhpay.backend.modules.corebanking.domain.transaction.Transaction;
import com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa.AccountRepository;
import com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa.LedgerEntryRepository;
import com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa.TransactionRepository;
import com.thinhpay.backend.shared.domain.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountApplicationService implements DepositUseCase, WithdrawUseCase {
    AccountRepository accountRepository;
    TransactionRepository transactionRepository;
    LedgerEntryRepository ledgerEntryRepository;

    @Override
    @Transactional
    public AccountResponse deposit(DepositRequest request) {
        log.info("Processing deposit: {}, user: {}", request.getRequestId(), request.getUserId());

        validateIdempotency(request.getRequestId());

        Account account = getAccountWithLock(request.getUserId());

        // Domain Logic
        account.credit(request.getAmount());

        // Record Keeping
        Transaction transaction = Transaction.createDeposit(account, request.getRequestId(), request.getAmount(), "Deposit via API");
        saveFlow(account, transaction, request.getAmount(), LedgerEntryType.CREDIT);

        return AccountResponse.from(account);
    }

    @Override
    @Transactional
    public AccountResponse withdraw(WithdrawRequest request) {
        log.info("Processing withdraw: {}, user: {}", request.getRequestId(), request.getUserId());

        validateIdempotency(request.getRequestId());

        Account account = getAccountWithLock(request.getUserId());

        // Domain Logic (Ném IllegalArgumentException nếu balance không đủ)
        account.debit(request.getAmount());

        // Record Keeping
        Transaction transaction = Transaction.createWithdraw(account, request.getRequestId(), request.getAmount(), "Withdraw via API");
        saveFlow(account, transaction, request.getAmount(), LedgerEntryType.DEBIT);

        return AccountResponse.from(account);
    }

    // Idempotency Check
    private void validateIdempotency(String requestId) {
        if (transactionRepository.existsByRequestId(requestId)) {
            throw new IllegalStateException("Transaction with request ID " + requestId + " already exists.");
        }
    }

    private Account getAccountWithLock(java.util.UUID userId) {
        return accountRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "userId", userId));
    }

    private void saveFlow(Account account, Transaction transaction, java.math.BigDecimal amount, LedgerEntryType type) {
        transaction.markAsCompleted();

        LedgerEntry ledgerEntry = LedgerEntry.create(
                transaction,
                account,
                amount,
                account.getBalance(),
                type
        );

        transactionRepository.save(transaction);
        accountRepository.save(account);
        ledgerEntryRepository.save(ledgerEntry);

        log.info("{} success. New balance: {}", type, account.getBalance());
    }
}
