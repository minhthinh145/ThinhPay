package com.thinhpay.backend.modules.iam.application.service;

import com.thinhpay.backend.modules.iam.application.port.in.LogoutUseCase;
import com.thinhpay.backend.modules.iam.application.port.out.SessionRepository;
import com.thinhpay.backend.modules.iam.application.port.out.TokenBlacklistRepository;
import com.thinhpay.backend.modules.iam.domain.security.IamSecurityLog;
import com.thinhpay.backend.modules.iam.domain.session.IamSession;
import com.thinhpay.backend.modules.iam.domain.token.IamTokenBlacklist;
import com.thinhpay.backend.modules.iam.domain.token.TokenType;
import com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa.IamSecurityLogRepository;
import com.thinhpay.backend.shared.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LogoutUserService implements LogoutUseCase {

    SessionRepository sessionRepository;
    TokenBlacklistRepository tokenBlacklistRepository;
    IamSecurityLogRepository securityLogRepository;

    @Override
    @Transactional
    public void logout(UUID userId, UUID sessionId) {
        IamSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId.toString()));

        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Session không thuộc về user này");
        }

        // 1. Invalidate session
        session.invalidate();
        sessionRepository.save(session);

        // 2. Blacklist access token
        IamTokenBlacklist blacklistEntry = IamTokenBlacklist.createForLogout(
            session.getSessionToken(), // JWT ID (jti)
            userId,
            TokenType.ACCESS,
            session.getExpiresAt()
        );
        tokenBlacklistRepository.save(blacklistEntry);

        // 3. Log security event
        IamSecurityLog securityLog = IamSecurityLog.logLogout(
            userId,
            session.getIpAddress(),
            session.getDeviceId()
        );
        securityLogRepository.save(securityLog);

        log.info("User logged out - userId: {}, sessionId: {}, deviceId: {}",
            userId, sessionId, session.getDeviceId());
    }

    @Override
    @Transactional
    public void logoutAll(UUID userId) {
        List<IamSession> activeSessions = sessionRepository.findByUserIdAndActiveTrue(userId);

        for (IamSession session : activeSessions) {
            // Invalidate session
            session.invalidate();
            sessionRepository.save(session);

            // Blacklist token
            IamTokenBlacklist blacklistEntry = IamTokenBlacklist.createForLogout(
                session.getSessionToken(),
                userId,
                TokenType.ACCESS,
                session.getExpiresAt()
            );
            tokenBlacklistRepository.save(blacklistEntry);
        }

        // Log security event (single log for logout all)
        IamSecurityLog securityLog = IamSecurityLog.builder()
            .userId(userId)
            .eventType(com.thinhpay.backend.modules.iam.domain.security.SecurityEventType.LOGOUT_ALL)
            .metadata(String.format("{\"sessions_count\":%d}", activeSessions.size()))
            .riskLevel(com.thinhpay.backend.modules.iam.domain.security.RiskLevel.LOW)
            .build();
        securityLogRepository.save(securityLog);

        log.info("User logged out from all devices - userId: {}, sessions: {}",
            userId, activeSessions.size());
    }
}
