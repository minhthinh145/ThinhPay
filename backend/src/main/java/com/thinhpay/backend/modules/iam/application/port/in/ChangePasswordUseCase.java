package com.thinhpay.backend.modules.iam.application.port.in;

import com.thinhpay.backend.modules.iam.application.dto.request.ChangePasswordRequest;

/**
 * Use Case cho đổi mật khẩu.
 */
public interface ChangePasswordUseCase {

    /**
     * Đổi mật khẩu của user.
     * Validate mật khẩu cũ trước khi đổi.
     */
    void changePassword(ChangePasswordRequest request);
}

