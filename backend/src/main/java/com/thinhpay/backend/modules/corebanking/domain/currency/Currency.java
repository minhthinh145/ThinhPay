package com.thinhpay.backend.modules.corebanking.domain.currency;

import com.thinhpay.backend.shared.domain.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;



@Entity
@Table(name = "core_currencies")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString()
public class Currency implements AggregateRoot {
    @Id
    @Size(max = 3)
    @EqualsAndHashCode.Include
    @Column(name = "code", nullable = false, length = 3, updatable = false)
    private String code;

    @Size(max = 5)
    @NotNull
    @Column(name = "symbol", nullable = false, length = 5)
    private String symbol;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "decimal_places")
    private Integer decimalPlaces = 0;

    public static Currency of(String code, String symbol, Integer decimalPlaces) {
        return Currency.builder()
                .code(code.toUpperCase())
                .symbol(symbol)
                .decimalPlaces(decimalPlaces)
                .build();
    }
}