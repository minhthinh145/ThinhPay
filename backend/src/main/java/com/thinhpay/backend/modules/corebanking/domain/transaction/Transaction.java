package com.thinhpay.backend.modules.corebanking.domain.transaction;

import com.thinhpay.backend.modules.corebanking.domain.account.Account;
import com.thinhpay.backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "core_transactions", indexes = {
        @Index(name = "idx_trx_request_id", columnList = "request_id", unique = true),
        @Index(name = "idx_trx_account_id", columnList = "account_id")
})
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    @ToString.Exclude
    Account account;

    @Size(max = 100)
    @NotNull
    @Column(name = "request_id", nullable = false, length = 100, unique = true, updatable = false)
    String requestId;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 19, scale = 4, updatable = false)
    BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20, updatable = false)
    TransactionType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    TransactionStatus status = TransactionStatus.PENDING;

    @Size(max = 2000)
    @Column(name = "metadata")
    String metadata;

    @Size(max = 1000)
    @Column(name = "description")
    String description;

    // ========== Factory Methods ========== //

    public static Transaction createDeposit(Account account, String requestId, BigDecimal amount, String description) {
        validateInputs(account, requestId, amount);
        return Transaction.builder()
                .account(account)
                .requestId(requestId)
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.PENDING)
                .description(description)
                .build();
    }

    public static Transaction createWithdraw(Account account, String requestId, BigDecimal amount, String description) {
        validateInputs(account, requestId, amount);
        return Transaction.builder()
                .account(account)
                .requestId(requestId)
                .amount(amount)
                .type(TransactionType.WITHDRAW)
                .status(TransactionStatus.PENDING)
                .description(description)
                .build();
    }

    // ========== Domain Methods ========== //

    public void markAsCompleted() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is already finished.");
        }
        this.status = TransactionStatus.COMPLETED;
    }

    public void markFailed(String reason) {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is already finished.");
        }
        this.status = TransactionStatus.FAILED;

        String failReason = " [FAILED: " + reason + "]";
        if (this.description == null) {
            this.description = failReason;
        } else {
            this.description += failReason;
        }
    }

    private static void validateInputs(Account account, String requestId, BigDecimal amount) {
        if (account == null)
            throw new IllegalArgumentException("Account cannot be null");
        if (requestId == null || requestId.isBlank())
            throw new IllegalArgumentException("Request ID is required");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");
    }
}