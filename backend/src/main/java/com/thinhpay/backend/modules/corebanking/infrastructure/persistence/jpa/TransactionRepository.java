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
    Optional<Transaction> findByRequestId(String requestId);

    boolean existsByRequestId(String requestId);

    Page<Transaction> findByAccountId(UUID accountId, Pageable pageable);
}
