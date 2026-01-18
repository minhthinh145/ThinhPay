package com.thinhpay.backend.modules.iam.application.port.in;

import com.thinhpay.backend.modules.iam.application.dto.response.SessionResponse;

import java.util.List;
import java.util.UUID;

/**
 * Use Case cho Session Management.
 * Quản lý các session (devices) của user.
 */
public interface SessionManagementUseCase {

    /**
     * Lấy danh sách tất cả active sessions của user.
     *
     * @param userId ID của user
     * @return Danh sách sessions
     */
    List<SessionResponse> getActiveSessions(UUID userId);

    /**
     * Terminate một session cụ thể.
     *
     * @param userId    ID của user
     * @param sessionId ID của session cần terminate
     */
    void terminateSession(UUID userId, UUID sessionId);

    /**
     * Terminate tất cả sessions của user (logout all devices).
     *
     * @param userId         ID của user
     * @param currentSessionId ID của session hiện tại (sẽ được giữ lại)
     */
    void terminateAllOtherSessions(UUID userId, UUID currentSessionId);

    /**
     * Get thông tin của session hiện tại.
     *
     * @param sessionId ID của session
     * @return Thông tin session
     */
    SessionResponse getCurrentSession(UUID sessionId);
}
