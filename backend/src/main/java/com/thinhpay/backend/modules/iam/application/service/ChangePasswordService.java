package com.thinhpay.backend.modules.iam.application.service;

import com.thinhpay.backend.modules.iam.application.dto.request.ChangePasswordRequest;
import com.thinhpay.backend.modules.iam.application.port.in.ChangePasswordUseCase;
import com.thinhpay.backend.modules.iam.application.port.out.UserRepository;
import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import com.thinhpay.backend.shared.exception.ResourceNotFoundException;
import com.thinhpay.backend.shared.exception.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý Change Password.
 * Tách riêng từ UserProfileService để tuân thủ Single Responsibility Principle.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChangePasswordService implements ChangePasswordUseCase {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        log.info("Changing password for user: {}", request.getUserId());

        // 1. Tìm user
        IamUser user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId().toString()));

        // 2. Validate password cũ
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            log.warn("Change password failed - incorrect current password for user: {}", request.getUserId());
            throw new ValidationException("Mật khẩu hiện tại không đúng");
        }

        // 3. Validate password mới không trùng password cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ValidationException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        // 4. Hash password mới
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());

        // 5. Gọi domain method để đổi password
        user.changePassword(newPasswordHash);

        // 6. Save
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", request.getUserId());
    }
}
