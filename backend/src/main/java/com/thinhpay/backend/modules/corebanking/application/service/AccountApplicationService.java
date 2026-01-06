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

    /**
     * Processes a deposit request: validates idempotency, locks and credits the account,
     * creates and completes a transaction, persists a ledger entry, and returns the updated account view.
     *
     * @param request the deposit request containing the userId, requestId (for idempotency), and amount to deposit
     * @return the updated AccountResponse reflecting the account's new balance and state
     * @throws IllegalStateException if a transaction with the same requestId already exists
     * @throws com.thinhpay.backend.common.exception.ResourceNotFoundException if no account exists for the given userId
     */
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

    /**
     * Processes a withdrawal request: validates idempotency, locks the account, debits the amount,
     * records a transaction and ledger entry, and returns the updated account state.
     *
     * @param request the withdrawal request containing userId, amount, and requestId for idempotency
     * @return the updated AccountResponse reflecting the account after the withdrawal
     * @throws IllegalStateException    if a transaction with the same requestId already exists
     * @throws com.thinhpay.backend.modules.corebanking.domain.ResourceNotFoundException
     *                                  if no account exists for the given userId
     * @throws IllegalArgumentException if the account has insufficient balance for the withdrawal
     */
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

    /**
     * Validate that no transaction with the given request identifier already exists.
     *
     * @param requestId the client-provided unique request identifier used for idempotency
     * @throws IllegalStateException if a transaction with the given requestId already exists
     */
    private void validateIdempotency(String requestId) {
        if (transactionRepository.existsByRequestId(requestId)) {
            throw new IllegalStateException("Transaction with request ID " + requestId + " already exists.");
        }
    }

    /**
     * Retrieve the account for the given user ID and acquire a database lock on it.
     *
     * @param userId the UUID of the user whose account should be fetched
     * @return the locked Account belonging to the specified user
     * @throws ResourceNotFoundException if no account exists for the given userId
     */
    private Account getAccountWithLock(java.util.UUID userId) {
        return accountRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "userId", userId));
    }

    /**
     * Finalizes a completed transaction by creating its ledger entry and persisting the transaction, account, and ledger entry.
     *
     * Marks the provided transaction as completed, builds a corresponding LedgerEntry reflecting the provided amount and the account's resulting balance, and saves the transaction, account, and ledger entry to their repositories.
     *
     * @param account     the account affected by the transaction
     * @param transaction the transaction to finalize and persist
     * @param amount      the monetary amount applied in the ledger entry
     * @param type        the ledger entry type (e.g., CREDIT or DEBIT)
     */
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