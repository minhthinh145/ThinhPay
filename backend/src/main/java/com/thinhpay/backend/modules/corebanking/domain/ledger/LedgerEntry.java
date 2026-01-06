package com.thinhpay.backend.modules.corebanking.domain.ledger;

import com.thinhpay.backend.modules.corebanking.domain.account.Account;
import com.thinhpay.backend.modules.corebanking.domain.transaction.Transaction;
import com.thinhpay.backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter(AccessLevel.PROTECTED)
@Entity
@Table(name = "core_ledger_entries", indexes = {
        @Index(name = "idx_ledger_trx_id", columnList = "transaction_id"),
        @Index(name = "idx_ledger_account_id", columnList = "account_id")
})
@SuperBuilder
@ToString(callSuper = true)
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "created_at"))
})
public class LedgerEntry extends BaseEntity {


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false, updatable = false)
    @ToString.Exclude
    private Transaction transaction;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    @ToString.Exclude
    private Account account;


    @NotNull
    @Column(name = "amount", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal amount;

    @NotNull
    @Column(name = "balance_snapshot", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal balanceSnapshot;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10, updatable = false)
    private LedgerEntryType type;

    // -- Factory Method -- //
    public static LedgerEntry create(Transaction transaction, Account account, BigDecimal amount, BigDecimal balanceSnapshot, LedgerEntryType type) {
        LedgerEntry entry = LedgerEntry.builder()
                .transaction(transaction)
                .account(account)
                .amount(amount)
                .balanceSnapshot(balanceSnapshot)
                .type(type)
                .build();

        entry.validate();
        return entry;
    }

    private void validate() {
        if (this.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ledger amount must be positive");
        }
        if (this.transaction == null || this.account == null) {
            throw new IllegalArgumentException("Ledger must belong to a transaction and account");
        }
    }
}