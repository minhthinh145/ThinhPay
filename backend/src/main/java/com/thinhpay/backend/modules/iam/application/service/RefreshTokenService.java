package com.thinhpay.backend.modules.iam.application.service;

import com.thinhpay.backend.modules.iam.application.dto.request.RefreshTokenRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.TokenResponse;
import com.thinhpay.backend.modules.iam.application.port.in.RefreshTokenUseCase;
import com.thinhpay.backend.modules.iam.application.port.out.RefreshTokenRepository;
import com.thinhpay.backend.modules.iam.domain.token.IamRefreshToken;
import com.thinhpay.backend.shared.exception.AuthenticationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenService implements RefreshTokenUseCase {

    RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        // 1. Tìm refresh token
        IamRefreshToken oldToken = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(AuthenticationException::invalidToken);

        // 2. Validate token
        if (!oldToken.isValid()) {
            throw AuthenticationException.tokenExpired();
        }

        if (oldToken.getRevoked()) {
            throw AuthenticationException.tokenBlacklisted();
        }

        // 3. Revoke old token (token rotation pattern)
        oldToken.revoke();
        refreshTokenRepository.save(oldToken);

        // 4. Generate new refresh token
        IamRefreshToken newToken = IamRefreshToken.create(
            oldToken.getUserId(),
            UUID.randomUUID().toString(),
            oldToken.getDeviceFingerprint(),
            7 // 7 days
        );
        refreshTokenRepository.save(newToken);

        // 5. TODO: Generate new access token
        String accessToken = "JWT_ACCESS_TOKEN_TODO"; // JwtTokenProvider.generateAccessToken(userId)

        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(newToken.getToken())
            .tokenType("Bearer")
            .expiresIn(900L) // 15 minutes
            .build();
    }
}
