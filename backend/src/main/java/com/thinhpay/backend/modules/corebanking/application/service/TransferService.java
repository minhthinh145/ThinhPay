package com.thinhpay.backend.modules.corebanking.application.service;

import com.thinhpay.backend.modules.corebanking.application.dto.req.TransferRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.res.TransferResponse;
import com.thinhpay.backend.modules.corebanking.application.port.in.TransferUseCase;
import com.thinhpay.backend.modules.corebanking.domain.account.Account;
import com.thinhpay.backend.modules.corebanking.domain.exchange.ExchangeRateService;
import com.thinhpay.backend.modules.corebanking.domain.ledger.LedgerEntry;
import com.thinhpay.backend.modules.corebanking.domain.ledger.LedgerEntryType;
import com.thinhpay.backend.modules.corebanking.domain.transaction.Transaction;
import com.thinhpay.backend.modules.corebanking.domain.transaction.TransactionType;
import com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa.AccountRepository;
import com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa.LedgerEntryRepository;
import com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa.TransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransferService implements TransferUseCase {
    AccountRepository accountRepository;
    TransactionRepository transactionRepository;
    LedgerEntryRepository ledgerEntryRepository;
    ExchangeRateService exchangeRateService;

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        log.info("Processing transfer: {} from {} to {}",
                request.getRequestId(), request.getSenderUserId(), request.getReceiverUserId());

        validateIdempotency(request.getRequestId());

        Account senderAccount = getAccountWithLock(request.getSenderUserId(), request.getCurrency());
        Account receiverAccount = getAccountWithLock(request.getReceiverUserId(), request.getCurrency());

        validateDifferentAccounts(senderAccount, receiverAccount);

        //Calculate amounts (multi-currency support)
        BigDecimal debitAmount = request.getAmount();
        BigDecimal creditAmount = calculateCreditAmount(
                debitAmount,
                senderAccount.getCurrency().getCode(),
                receiverAccount.getCurrency().getCode()
        );

        executeTransfer(senderAccount, receiverAccount, debitAmount, creditAmount);

        Transaction senderTransaction = createAndSaveTransaction(
                senderAccount, request, debitAmount, TransactionType.TRANSFER_OUT);
        Transaction receiverTransaction = createAndSaveTransaction(
                receiverAccount, request, creditAmount, TransactionType.TRANSFER_IN);


        createAndSaveLedgerEntries(senderTransaction, receiverTransaction,
                senderAccount, receiverAccount, debitAmount, creditAmount);

        log.info("Transfer completed: {} - Sender balance: {}, Receiver balance: {}",
                request.getRequestId(), senderAccount.getBalance(), receiverAccount.getBalance());

        return TransferResponse.from(senderTransaction, receiverTransaction);
    }

    private void validateIdempotency(String requestId) {
        if (transactionRepository.existsByRequestId(requestId)) {
            throw new IllegalStateException("Transaction with request ID " + requestId + " already exists.");
        }
    }

    private Account getAccountWithLock(UUID userId, String currency) {
        return accountRepository.findByUserIdAndCurrencyCodeWithLock(userId, currency)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found for user: " + userId + " with currency: " + currency
                ));
    }

    private void validateDifferentAccounts(Account sender, Account receiver) {
        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Sender and receiver accounts must be different");
        }
    }

    private BigDecimal calculateCreditAmount(BigDecimal debitAmount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return debitAmount;
        }

        log.info("Converting {} {} to {}", debitAmount, fromCurrency, toCurrency);
        BigDecimal converted = exchangeRateService.convert(debitAmount, fromCurrency, toCurrency);
        log.info("Converted amount: {} {}", converted, toCurrency);
        return converted;
    }

    private void executeTransfer(Account senderAccount, Account receiverAccount,
                                 BigDecimal debitAmount, BigDecimal creditAmount) {
        senderAccount.debit(debitAmount);
        receiverAccount.credit(creditAmount);

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);
    }

    private Transaction createAndSaveTransaction(Account account, TransferRequest request,
                                                 BigDecimal amount, TransactionType type) {
        String requestId = type == TransactionType.TRANSFER_OUT
                ? request.getRequestId()
                : request.getRequestId() + "-IN";

        Transaction transaction = Transaction.builder()
                .account(account)
                .requestId(requestId)
                .amount(amount)
                .type(type)
                .description(request.getDescription())
                .build();

        transaction.markAsCompleted();
        return transactionRepository.save(transaction);
    }

    private void createAndSaveLedgerEntries(Transaction senderTransaction, Transaction receiverTransaction,
                                            Account senderAccount, Account receiverAccount,
                                            BigDecimal debitAmount, BigDecimal creditAmount) {
        LedgerEntry senderLedger = LedgerEntry.create(
                senderTransaction, senderAccount, debitAmount,
                senderAccount.getBalance(), LedgerEntryType.DEBIT);

        LedgerEntry receiverLedger = LedgerEntry.create(
                receiverTransaction, receiverAccount, creditAmount,
                receiverAccount.getBalance(), LedgerEntryType.CREDIT);

        ledgerEntryRepository.save(senderLedger);
        ledgerEntryRepository.save(receiverLedger);
    }

}
