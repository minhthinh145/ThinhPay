package com.thinhpay.backend.modules.corebanking.application.dto.res;

import com.thinhpay.backend.modules.corebanking.domain.transaction.Transaction;
import com.thinhpay.backend.modules.corebanking.domain.transaction.TransactionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferResponse {
    //Transaction IDs
    UUID senderTransactionId;
    UUID receiverTransactionId;
    String requestId;

    //Account IDs
    UUID senderAccountId;
    UUID receiverAccountId;

    //Multi-currency support
    BigDecimal debitAmount;
    BigDecimal creditAmount;

    //Currencies
    String fromCurrency;
    String toCurrency;

    //Exchange rate
    BigDecimal exchangeRate;

    //Status and metadata
    TransactionStatus status;
    Instant completedAt;
    String description;



    // Factory method for creating TransferResponse from Transaction
    public static TransferResponse from(Transaction senderTransaction, Transaction receiverTransaction) {
        BigDecimal debitAmount = senderTransaction.getAmount();
        BigDecimal creditAmount = receiverTransaction.getAmount();

        // Calculate exchange rate (handle same currency case)
        BigDecimal exchangeRate = creditAmount.divide(debitAmount, 6, RoundingMode.HALF_UP);

        return com.thinhpay.backend.modules.corebanking.application.dto.res.TransferResponse.builder()
                .senderTransactionId(senderTransaction.getId())
                .receiverTransactionId(receiverTransaction.getId())
                .requestId(senderTransaction.getRequestId())
                .senderAccountId(senderTransaction.getAccount().getId())
                .receiverAccountId(receiverTransaction.getAccount().getId())
                .debitAmount(debitAmount)
                .creditAmount(creditAmount)
                .fromCurrency(senderTransaction.getAccount().getCurrency().getCode())
                .toCurrency(receiverTransaction.getAccount().getCurrency().getCode())
                .exchangeRate(exchangeRate)
                .status(senderTransaction.getStatus())
                .completedAt(senderTransaction.getCreatedAt())
                .description(senderTransaction.getDescription())
                .build();
    }
}
