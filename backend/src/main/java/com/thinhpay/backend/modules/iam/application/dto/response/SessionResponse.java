package com.thinhpay.backend.modules.iam.application.dto.response;

import com.thinhpay.backend.modules.iam.domain.session.IamSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class SessionResponse {

    UUID sessionId;
    UUID userId;
    String deviceId;
    String deviceType;
    String deviceName;
    String ipAddress;
    String userAgent;
    String location;
    Boolean active;
    Boolean isCurrent;
    Instant createdAt;
    Instant lastActivityAt;
    Instant expiresAt;

    /**
     * Factory method từ domain entity.
     */
    public static SessionResponse from(IamSession session) {
        return from(session, false);
    }

    /**
     * Factory method từ domain entity với current flag.
     */
    public static SessionResponse from(IamSession session, boolean isCurrent) {
        return SessionResponse.builder()
            .sessionId(session.getId())
            .userId(session.getUserId())
            .deviceId(session.getDeviceId())
            .deviceType(extractDeviceType(session.getUserAgent()))
            .deviceName(extractDeviceName(session.getUserAgent()))
            .ipAddress(session.getIpAddress())
            .userAgent(session.getUserAgent())
            .location(null) // TODO: Implement IP geolocation
            .active(!session.isExpired())
            .isCurrent(isCurrent)
            .createdAt(session.getCreatedAt())
            .lastActivityAt(session.getLastActivityAt())
            .expiresAt(session.getExpiresAt())
            .build();
    }

    /**
     * Extract device type from user agent string.
     */
    private static String extractDeviceType(String userAgent) {
        if (userAgent == null) return "UNKNOWN";

        if (userAgent.toLowerCase().contains("mobile")) return "MOBILE";
        if (userAgent.toLowerCase().contains("tablet")) return "TABLET";

        return "DESKTOP";
    }

    /**
     * Extract device name from user agent string.
     */
    private static String extractDeviceName(String userAgent) {
        if (userAgent == null) return "Unknown Device";

        if (userAgent.contains("Chrome")) return "Chrome Browser";
        if (userAgent.contains("Firefox")) return "Firefox Browser";
        if (userAgent.contains("Safari")) return "Safari Browser";
        if (userAgent.contains("Edge")) return "Edge Browser";
        if (userAgent.contains("Mobile")) return "Mobile Device";

        return "Web Browser";
    }
}

