package com.thinhpay.backend.modules.iam.application.service;

import com.thinhpay.backend.modules.iam.application.dto.response.SessionResponse;
import com.thinhpay.backend.modules.iam.application.port.in.SessionManagementUseCase;
import com.thinhpay.backend.modules.iam.application.port.out.SessionRepository;
import com.thinhpay.backend.modules.iam.domain.session.IamSession;
import com.thinhpay.backend.shared.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service xử lý Session Management.
 * Quản lý các active sessions của user (multi-device login).
 */
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionManagementService implements SessionManagementUseCase {

    SessionRepository sessionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getActiveSessions(UUID userId) {
        log.info("Getting active sessions for user: {}", userId);

        List<IamSession> sessions = sessionRepository.findActiveSessionsByUserId(userId);

        return sessions.stream()
            .map(SessionResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void terminateSession(UUID userId, UUID sessionId) {
        log.info("Terminating session {} for user {}", sessionId, userId);

        // 1. Tìm session
        IamSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId.toString()));

        // 2. Verify session thuộc về user này
        if (!session.getUserId().equals(userId)) {
            log.warn("User {} attempted to terminate session {} of another user", userId, sessionId);
            throw new IllegalArgumentException("Không có quyền terminate session này");
        }

        // 3. Invalidate session
        session.invalidate();
        sessionRepository.save(session);

        // Note: JWT blacklisting sẽ được xử lý bởi LogoutService
        // Session chỉ quản lý lifecycle của session entity

        log.info("Session {} terminated successfully", sessionId);
    }

    @Override
    @Transactional
    public void terminateAllOtherSessions(UUID userId, UUID currentSessionId) {
        log.info("Terminating all sessions except {} for user {}", currentSessionId, userId);

        List<IamSession> sessions = sessionRepository.findActiveSessionsByUserId(userId);

        int terminatedCount = 0;
        for (IamSession session : sessions) {
            // Skip current session
            if (session.getId().equals(currentSessionId)) {
                continue;
            }

            // Invalidate session
            session.invalidate();
            sessionRepository.save(session);


            terminatedCount++;
        }

        log.info("Terminated {} sessions for user {}", terminatedCount, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponse getCurrentSession(UUID sessionId) {
        log.debug("Getting current session: {}", sessionId);

        IamSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId.toString()));

        return SessionResponse.from(session, true);
    }
}
