package com.thinhpay.backend.modules.iam.application.port.in;

import java.util.UUID;

public interface LogoutUseCase {
    void logout(UUID userId, UUID sessionId);
    void logoutAll(UUID userId);
}
