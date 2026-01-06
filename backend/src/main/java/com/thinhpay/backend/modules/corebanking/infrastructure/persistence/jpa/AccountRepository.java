package com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.corebanking.domain.account.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.userId = :userId")
    Optional<Account> findByUserIdWithLock(@Param("userId") UUID userId);
}
