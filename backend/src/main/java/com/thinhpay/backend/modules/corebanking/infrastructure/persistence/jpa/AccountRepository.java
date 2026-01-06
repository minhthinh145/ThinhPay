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
    /**
 * Finds the Account associated with the given user ID.
 *
 * @param userId the UUID of the user whose Account is being searched
 * @return an Optional containing the Account for the given user ID, or empty if none exists
 */
Optional<Account> findByUserId(UUID userId);

    /**
 * Checks whether an Account exists for the specified user ID.
 *
 * @param userId the UUID of the user to check for an associated Account
 * @return `true` if an Account exists for the given `userId`, `false` otherwise
 */
boolean existsByUserId(UUID userId);

    /**
     * Retrieve the Account for the given userId while acquiring a pessimistic write lock.
     *
     * @param userId the UUID of the user whose Account to retrieve
     * @return an Optional containing the Account associated with the given userId, or empty if none exists
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.userId = :userId")
    Optional<Account> findByUserIdWithLock(@Param("userId") UUID userId);
}