package com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa;

import com.thinhpay.backend.modules.iam.domain.role.IamRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IamRoleRepository extends JpaRepository<IamRole, String> {

    /**
     * Tìm role theo ID.
     * IDs: 'USER', 'ADMIN', 'MERCHANT', 'AGENT'
     */
    Optional<IamRole> findById(String id);

    /**
     * Check role có tồn tại không.
     * Dùng để validate khi assign role cho user.
     */
    boolean existsById(String id);
}
