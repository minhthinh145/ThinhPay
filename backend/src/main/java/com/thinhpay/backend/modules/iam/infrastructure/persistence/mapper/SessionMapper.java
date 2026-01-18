package com.thinhpay.backend.modules.iam.infrastructure.persistence.mapper;

import com.thinhpay.backend.modules.iam.application.dto.response.SessionResponse;
import com.thinhpay.backend.modules.iam.domain.session.IamSession;

public class SessionMapper {

    public SessionResponse toResponse(IamSession session) {
        return SessionResponse.builder()
                .sessionId(session.getId())
                .deviceId(session.getDeviceId())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .active(session.getActive())
                .lastActivityAt(session.getLastActivityAt())
                .expiresAt(session.getExpiresAt())
                .build();
    }
}
