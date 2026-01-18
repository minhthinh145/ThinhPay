package com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import com.thinhpay.backend.modules.iam.domain.user.UserAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho IamUser entity.
 * Provides queries for authentication, user management, and admin operations.
 */
@Repository
public interface IamUserRepository extends JpaRepository<IamUser, UUID> {

    /**
     * Tìm user theo email.
     * CRITICAL: Login flow với email/password.
     *
     * Note: Query trực tiếp column name thay vì embedded object path
     */
    @Query("SELECT u FROM IamUser u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<IamUser> findByEmail(@Param("email") String email);

    /**
     * Tìm user theo phone number.
     * Dùng cho SMS OTP login hoặc phone-based authentication.
     *
     * Note: Query trực tiếp column name thay vì embedded object path
     */
    @Query("SELECT u FROM IamUser u WHERE u.phoneNumber = :phoneNumber")
    Optional<IamUser> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Check email đã tồn tại chưa.
     * Dùng cho registration validation.
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM IamUser u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Check phone number đã tồn tại chưa.
     * Dùng cho registration validation.
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM IamUser u WHERE u.phoneNumber = :phoneNumber")
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Tìm users theo role.
     * Dùng cho admin panel - list users by role.
     */
    @Query("SELECT u FROM IamUser u WHERE u.roleId = :roleId")
    List<IamUser> findByRoleId(@Param("roleId") String roleId);

    /**
     * Tìm users theo status.
     * Dùng cho admin panel - list active/suspended/locked users.
     */
    @Query("SELECT u FROM IamUser u WHERE u.status = :status")
    List<IamUser> findByStatus(@Param("status") UserAccountStatus status);

    /**
     * Tìm users theo role và status.
     * Dùng cho admin panel filtering.
     */
    @Query("SELECT u FROM IamUser u WHERE u.roleId = :roleId AND u.status = :status")
    List<IamUser> findByRoleIdAndStatus(@Param("roleId") String roleId, @Param("status") UserAccountStatus status);

    /**
     * Đếm số lượng users theo role.
     * Analytics/statistics.
     */
    long countByRoleId(String roleId);

    /**
     * Đếm số lượng users theo status.
     * Analytics/statistics.
     */
    long countByStatus(UserAccountStatus status);

    /**
     * Tìm users chưa verify email.
     * Dùng cho reminder emails.
     */
    @Query("SELECT u FROM IamUser u WHERE u.emailVerified = false")
    List<IamUser> findUsersWithUnverifiedEmail();

    /**
     * Tìm users chưa verify phone.
     * Dùng cho reminder SMS.
     */
    @Query("SELECT u FROM IamUser u WHERE u.phoneVerified = false")
    List<IamUser> findUsersWithUnverifiedPhone();

    /**
     * Tìm user theo email hoặc phone number (dùng cho login với identifier).
     * IamUser không có username field, sử dụng email làm identifier chính.
     */
    @Query("SELECT u FROM IamUser u WHERE LOWER(u.email) = LOWER(:identifier) OR u.phoneNumber = :identifier")
    Optional<IamUser> findByEmailOrPhoneNumber(@Param("identifier") String identifier);
}
