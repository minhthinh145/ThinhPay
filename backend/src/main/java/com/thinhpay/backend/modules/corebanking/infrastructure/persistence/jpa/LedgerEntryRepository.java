package com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.corebanking.domain.ledger.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    /**
 * Retrieves all ledger entries associated with the specified transaction.
 *
 * @param transactionId the UUID of the transaction whose ledger entries are requested
 * @return a list of LedgerEntry entities linked to the given transactionId; empty if none are found
 */
List<LedgerEntry> findByTransactionId(UUID transactionId);

    /**
 * Counts ledger entries for a given account.
 *
 * @param accountId the UUID of the account whose ledger entries are counted
 * @return the number of ledger entries associated with the given account
 */
int countByAccountId(UUID accountId);
}