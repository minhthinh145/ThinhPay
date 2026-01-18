package com.thinhpay.backend.modules.iam.application.port.in;

import com.thinhpay.backend.modules.iam.application.dto.request.UpdateProfileRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.UserProfileResponse;

import java.util.UUID;

/**
 * Use Case cho User Profile.
 * Gom các chức năng liên quan đến profile của user.
 */
public interface UserProfileUseCase {

    /**
     * Lấy thông tin profile của user.
     */
    UserProfileResponse getUserProfile(UUID userId);

    /**
     * Cập nhật profile của user.
     */
    UserProfileResponse updateProfile(UpdateProfileRequest request);
}

