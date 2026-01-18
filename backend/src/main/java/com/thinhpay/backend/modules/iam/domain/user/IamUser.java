package com.thinhpay.backend.modules.iam.domain.user;

import com.thinhpay.backend.shared.domain.AggregateRoot;
import com.thinhpay.backend.shared.domain.BaseEntity;
import com.thinhpay.backend.shared.domain.Email;
import com.thinhpay.backend.shared.domain.PhoneNumber;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;


@Entity
@Table(name = "iam_users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_phone", columnList = "phone_number", unique = true),
        @Index(name = "idx_users_status", columnList = "status"),
        @Index(name = "idx_users_role", columnList = "role_id"),
    }
)
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString(callSuper = true, exclude = {"passwordHash", "pinHash"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IamUser extends BaseEntity implements AggregateRoot {

    @NotNull
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true, length = 255))
    Email email;

   @NotNull
   @Embedded
   @AttributeOverride(name = "value", column = @Column(name = "phone_number", nullable = false, unique = true, length = 20))
   PhoneNumber phoneNumber;

   @Size(max = 255)
   @NotNull
   @Column(name = "password_hash", nullable = false)
   String passwordHash;

   @Size(max = 255)
   @Column(name = "pin_hash")
   String pinHash;

   @Size(max = 255)
   @NotNull
   @Column(name = "full_name", nullable = false)
   String fullName;

   @Size(max = 500)
   @Column(name = "avatar_url", length = 500)
   String avatarUrl;

   @NotNull
   @Enumerated(EnumType.STRING)
   @ColumnDefault("'PENDING_VERIFICATION'")
   @Column(name = "status", nullable = false, length = 20)
   @Builder.Default
   UserAccountStatus status = UserAccountStatus.PENDING_VERIFICATION;

   @Size(max = 20)
   @NotNull
   @ColumnDefault("'USER'")
   @Column(name = "role_id", nullable = false, length = 20)
   @Builder.Default
   String roleId = "USER";

   @ColumnDefault("false")
   @Column(name = "email_verified", nullable = false)
   @Builder.Default
   Boolean emailVerified = false;

   @ColumnDefault("false")
   @Column(name = "phone_verified", nullable = false)
   @Builder.Default
   Boolean phoneVerified = false;

   @ColumnDefault("false")
   @Column(name = "kyc_verified", nullable = false)
   @Builder.Default
   Boolean kycVerified = false;

   @NotNull
   @Enumerated(EnumType.STRING)
   @ColumnDefault("'BASIC'")
   @Column(name = "kyc_level", nullable = false, length = 20)
   @Builder.Default
   KycLevel kycLevel = KycLevel.BASIC;

   // Factory method
    public static  IamUser createNew(String emailValue, String phoneNumber,
                                     String passwordHash, String fullName){
        if(passwordHash == null || passwordHash.isBlank()){
            throw new IllegalArgumentException("Password hash không được để trống");
        }
        if(fullName == null || fullName.isBlank()){
            throw new IllegalArgumentException("Họ tên không được để trống");
        }

        return IamUser.builder()
                .email(new Email(emailValue))
                .phoneNumber(new PhoneNumber(phoneNumber))
                .passwordHash(passwordHash)
                .fullName(fullName.trim())
                .status(UserAccountStatus.PENDING_VERIFICATION)
                .roleId("USER")
                .emailVerified(false)
                .phoneVerified(false)
                .kycVerified(false)
                .kycLevel(KycLevel.BASIC)
                .build();
    }

    // Domain methods

    public void verifyEmail(){
        if(this.emailVerified){
            throw new IllegalStateException("Email đã được xác thực");
        }
        this.emailVerified = true;
        checkAndActivateAccount();
    }

    public void verifyPhone() {
        if (this.phoneVerified) {
            throw new IllegalStateException("Phone đã được xác thực");
        }
        this.phoneVerified = true;
        checkAndActivateAccount();
    }

    public void upgradeKycLevel(KycLevel newLevel) {
        if (newLevel == null) {
            throw new IllegalArgumentException("KYC level không được để trống");
        }
        if (newLevel.ordinal() < this.kycLevel.ordinal()) {
            throw new IllegalArgumentException("Không thể hạ cấp KYC");
        }
        this.kycLevel = newLevel;
        this.kycVerified = true;
    }

    public void setPinHash(String pinHash) {
        if (pinHash == null || pinHash.isBlank()) {
            throw new IllegalArgumentException("PIN hash không được để trống");
        }
        this.pinHash = pinHash;
    }

    public void changePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("Password không hợp lệ");
        }
        this.passwordHash = newPasswordHash;
    }

    public void lock(String reason) {
        if (this.status == UserAccountStatus.CLOSED) {
            throw new IllegalStateException("Không thể khóa tài khoản đã đóng");
        }
        this.status = UserAccountStatus.LOCKED;
    }


    public void suspend(String reason) {
        if (this.status == UserAccountStatus.CLOSED) {
            throw new IllegalStateException("Không thể suspend tài khoản đã đóng");
        }
        this.status = UserAccountStatus.SUSPENDED;
    }

    public void reactivate() {
        if (this.status == UserAccountStatus.CLOSED) {
            throw new IllegalStateException("Không thể reactivate tài khoản đã đóng");
        }
        if (!this.emailVerified || !this.phoneVerified) {
            throw new IllegalStateException("Phải verify email và phone trước");
        }
        this.status = UserAccountStatus.ACTIVE;
    }

    public void close() {
        this.status = UserAccountStatus.CLOSED;
    }

    public void updateProfile(String fullName, String avatarUrl) {
        if (fullName != null && !fullName.isBlank()) {
            this.fullName = fullName.trim();
        }
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl.trim();
        }
    }

    private void checkAndActivateAccount(){
        if(this.emailVerified && this.phoneVerified
        && this.status == UserAccountStatus.PENDING_VERIFICATION){
            this.status = UserAccountStatus.ACTIVE;
        }
    }

    // Bussiness logic

    public boolean canLogin() {
        return this.status == UserAccountStatus.ACTIVE;
    }

    public boolean canPerformTransaction() {
        return this.status == UserAccountStatus.ACTIVE &&
                this.emailVerified && this.phoneVerified;
    }

    public boolean isFullyVerified() {
        return this.emailVerified && this.phoneVerified && this.kycVerified;
    }

    public String getEmailValue() {
        return this.email.getValue();
    }

    public String getPhoneNumberValue() {
        return this.phoneNumber.getValue();
    }

    public String getPhoneValue() {
        return this.phoneNumber.getValue();
    }
}