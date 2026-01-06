package com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.corebanking.domain.ledger.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByTransactionId(UUID transactionId);

    int countByAccountId(UUID accountId);
}
