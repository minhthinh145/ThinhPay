package com.thinhpay.backend.modules.iam.infrastructure.persistence.mapper;

import com.thinhpay.backend.modules.iam.application.dto.response.UserProfileResponse;
import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import org.springframework.stereotype.Component;

/**
 * Mapper cho User entity và DTOs.
 * Tách biệt logic mapping ra khỏi entity/DTO.
 */
@Component
public class    UserMapper {

    /**
     * Convert IamUser entity sang UserProfileResponse DTO.
     */
    public UserProfileResponse toProfileResponse(IamUser user) {
        if (user == null) {
            return null;
        }

        return UserProfileResponse.builder()
            .id(user.getId())
            .email(user.getEmailValue())
            .phoneNumber(user.getPhoneNumberValue())
            .fullName(user.getFullName())
            .avatarUrl(user.getAvatarUrl())
            .status(user.getStatus())
            .roleId(user.getRoleId())
            .roleName(user.getRoleId()) // TODO: Join với IamRole để lấy roleName
            .emailVerified(user.getEmailVerified())
            .phoneVerified(user.getPhoneVerified())
            .kycVerified(user.getKycVerified())
            .kycLevel(user.getKycLevel())
            .createdAt(user.getCreatedAt())
            .build();
    }

    // TODO: Thêm các mapping methods khác khi cần:
    // - toEntity(RegisterRequest) -> IamUser
    // - toListResponse(List<IamUser>) -> List<UserProfileResponse>
}

