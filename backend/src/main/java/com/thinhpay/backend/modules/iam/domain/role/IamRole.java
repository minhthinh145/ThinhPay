package com.thinhpay.backend.modules.iam.domain.role;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

/**
 * Reference Data - Vai trò người dùng trong hệ thống.
 *
 * Roles:
 * - USER: Khách hàng cá nhân (transfer, deposit, withdraw, view_balance, view_transactions)
 * - MERCHANT: Tài khoản doanh nghiệp (receive_payment, qr_generate, refund, view_revenue, export_report)
 * - AGENT: Đại lý nạp/rút (cash_in, cash_out, kyc_verify, commission_view)
 * - ADMIN: Quản trị viên (all permissions)
 *
 * Note: Đây là reference data table - seed data cố định, ít khi thay đổi.
 * Permissions được lưu dạng JSON array trong TEXT field.
 */
@Entity
@Table(name = "iam_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IamRole {

    @Id
    @Size(max = 20)
    @Column(name = "id", nullable = false, length = 20)
    private String id;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ========== Domain Methods ========== //

    /**
     * Check xem role có phải ADMIN không.
     * Admin có tất cả quyền trong hệ thống.
     */
    public boolean isAdmin() {
        return "ADMIN".equals(this.id);
    }

    /**
     * Check xem role có phải USER không.
     * User là khách hàng cá nhân.
     */
    public boolean isUser() {
        return "USER".equals(this.id);
    }

    /**
     * Check xem role có phải MERCHANT không.
     * Merchant là tài khoản doanh nghiệp.
     */
    public boolean isMerchant() {
        return "MERCHANT".equals(this.id);
    }

    /**
     * Check xem role có phải AGENT không.
     * Agent là đại lý nạp/rút tiền.
     */
    public boolean isAgent() {
        return "AGENT".equals(this.id);
    }

    /**
     * Check xem role có phải loại được chỉ định không.
     *
     * @param roleType Role type cần check (USER, ADMIN, MERCHANT, AGENT)
     * @return true nếu match
     */
    public boolean hasRoleType(String roleType) {
        return this.id.equals(roleType);
    }

    /**
     * Get role hierarchy level.
     * Dùng để so sánh quyền hạn giữa các roles.
     *
     * @return Level càng cao càng có nhiều quyền (4 = ADMIN, 1 = USER)
     */
    public int getHierarchyLevel() {
        return switch (this.id) {
            case "ADMIN" -> 4;
            case "AGENT" -> 3;
            case "MERCHANT" -> 2;
            case "USER" -> 1;
            default -> 0;
        };
    }

    /**
     * Check xem role này có quyền cao hơn role khác không.
     *
     * @param otherRole Role cần so sánh
     * @return true nếu role này có hierarchy cao hơn
     */
    public boolean hasHigherAuthorityThan(IamRole otherRole) {
        return this.getHierarchyLevel() > otherRole.getHierarchyLevel();
    }

    /**
     * Check xem role này có quyền bằng hoặc cao hơn role khác không.
     *
     * @param otherRole Role cần so sánh
     * @return true nếu role này có hierarchy >= otherRole
     */
    public boolean hasEqualOrHigherAuthorityThan(IamRole otherRole) {
        return this.getHierarchyLevel() >= otherRole.getHierarchyLevel();
    }
}