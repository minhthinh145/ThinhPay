package com.thinhpay.backend.modules.iam.application.service;

import com.thinhpay.backend.modules.iam.application.dto.request.UpdateProfileRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.UserProfileResponse;
import com.thinhpay.backend.modules.iam.application.port.in.UserProfileUseCase;
import com.thinhpay.backend.modules.iam.application.port.out.UserRepository;
import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import com.thinhpay.backend.shared.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service xử lý User Profile.
 * Inject UserRepository port (domain interface), KHÔNG inject JPA repository trực tiếp.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileService implements UserProfileUseCase {

    UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        IamUser user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        return UserProfileResponse.from(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        IamUser user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId().toString()));

        // Gọi domain method để update profile
        user.updateProfile(request.getFullName(), request.getAvatarUrl());

        IamUser updatedUser = userRepository.save(user);
        return UserProfileResponse.from(updatedUser);
    }
}

