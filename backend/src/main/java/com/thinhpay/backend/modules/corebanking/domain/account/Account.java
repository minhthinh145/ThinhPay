package com.thinhpay.backend.modules.corebanking.domain.account;

import com.thinhpay.backend.modules.corebanking.domain.currency.Currency;
import com.thinhpay.backend.shared.domain.AggregateRoot;
import com.thinhpay.backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "core_accounts")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account extends BaseEntity implements AggregateRoot {

    @NotNull
    @Column(name = "user_id", nullable = false, updatable = false)
    UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_code", nullable = false)
    @ToString.Exclude
    Currency currency;

    @NotNull
    @Builder.Default
    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    BigDecimal balance = BigDecimal.ZERO;

    @NotNull
    @Builder.Default
    @Column(name = "held_balance", nullable = false, precision = 19, scale = 4)
    BigDecimal heldBalance = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    AccountStatus status = AccountStatus.ACTIVE;

    // ========== Factory Method ========== //

    public static Account open(UUID userId, Currency currency) {
        if (userId == null)
            throw new IllegalArgumentException("User ID cannot be null");
        if (currency == null)
            throw new IllegalArgumentException("Currency cannot be null");

        return Account.builder()
                .userId(userId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .heldBalance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    // ========== Domain Methods ========== //

    public BigDecimal getAvailableBalance() {
        return balance.subtract(heldBalance);
    }

    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount to credit must be non-negative");
        }
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount to debit must be positive");
        }
        if (getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient available balance");
        }
        this.balance = this.balance.subtract(amount);
    }
}