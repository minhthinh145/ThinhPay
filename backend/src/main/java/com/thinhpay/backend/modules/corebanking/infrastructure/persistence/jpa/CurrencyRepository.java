package com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.corebanking.domain.currency.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, String> {
}
