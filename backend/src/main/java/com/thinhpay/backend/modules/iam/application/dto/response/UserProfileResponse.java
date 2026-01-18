package com.thinhpay.backend.modules.iam.application.dto.response;

import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import com.thinhpay.backend.modules.iam.domain.user.KycLevel;
import com.thinhpay.backend.modules.iam.domain.user.UserAccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class UserProfileResponse {

    UUID id;
    String email;
    String phoneNumber;
    String fullName;
    String avatarUrl;
    UserAccountStatus status;
    String roleId;
    String roleName;
    Boolean emailVerified;
    Boolean phoneVerified;
    Boolean kycVerified;
    KycLevel kycLevel;
    Instant createdAt;

    /**
     * Factory method - Tạo UserProfileResponse từ IamUser entity.
     */
    public static UserProfileResponse from(IamUser user) {
        return UserProfileResponse.builder()
            .id(user.getId())
            .email(user.getEmailValue())
            .phoneNumber(user.getPhoneValue())
            .fullName(user.getFullName())
            .avatarUrl(user.getAvatarUrl())
            .status(user.getStatus())
            .roleId(user.getRoleId())
            .roleName(user.getRoleId()) // TODO: Load role name from RoleRepository
            .emailVerified(user.getEmailVerified())
            .phoneVerified(user.getPhoneVerified())
            .kycVerified(user.getKycVerified())
            .kycLevel(user.getKycLevel())
            .createdAt(user.getCreatedAt())
            .build();
    }
}


