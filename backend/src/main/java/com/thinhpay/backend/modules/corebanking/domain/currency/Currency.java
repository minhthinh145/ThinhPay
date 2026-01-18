package com.thinhpay.backend.modules.corebanking.domain.currency;

import com.thinhpay.backend.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "core_currencies")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Currency implements AggregateRoot {

    @Id
    @Size(max = 3)
    @EqualsAndHashCode.Include
    @Column(name = "code", nullable = false, length = 3, updatable = false)
    String code;

    @Size(max = 5)
    @NotNull
    @Column(name = "symbol", nullable = false, length = 5)
    String symbol;

    @NotNull
    @Builder.Default
    @Column(name = "decimal_places")
    Integer decimalPlaces = 0;

    // ========== Factory Method ========== //

    public static Currency of(String code, String symbol, Integer decimalPlaces) {
        return Currency.builder()
                .code(code.toUpperCase())
                .symbol(symbol)
                .decimalPlaces(decimalPlaces)
                .build();
    }
}