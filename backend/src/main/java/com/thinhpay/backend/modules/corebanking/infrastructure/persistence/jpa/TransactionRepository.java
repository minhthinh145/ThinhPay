package com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.corebanking.domain.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    /**
 * Finds a transaction by its external request identifier.
 *
 * @param requestId the external request identifier associated with the transaction
 * @return an Optional containing the Transaction if found, otherwise an empty Optional
 */
Optional<Transaction> findByRequestId(String requestId);

    /**
 * Check whether a Transaction with the given requestId exists.
 *
 * @param requestId the external or client-provided request identifier to look up
 * @return `true` if a Transaction with the given requestId exists, `false` otherwise
 */
boolean existsByRequestId(String requestId);

    /**
 * Retrieves a paginated list of transactions for the specified account.
 *
 * @param accountId the UUID of the account whose transactions are requested
 * @param pageable  pagination and sorting information for the result page
 * @return a page of Transaction entities belonging to the given account as defined by the Pageable
 */
Page<Transaction> findByAccountId(UUID accountId, Pageable pageable);
}