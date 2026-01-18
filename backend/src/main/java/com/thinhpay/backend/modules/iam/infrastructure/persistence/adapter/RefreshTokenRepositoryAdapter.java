package com.thinhpay.backend.modules.iam.infrastructure.persistence.adapter;

import com.thinhpay.backend.modules.iam.application.port.out.RefreshTokenRepository;
import com.thinhpay.backend.modules.iam.domain.token.IamRefreshToken;
import com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa.IamRefreshTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implement RefreshTokenRepository port.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    IamRefreshTokenRepository jpaRepository;

    @Override
    public IamRefreshToken save(IamRefreshToken refreshToken) {
        return jpaRepository.save(refreshToken);
    }

    @Override
    public Optional<IamRefreshToken> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<IamRefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token);
    }

    @Override
    public List<IamRefreshToken> findByUserIdAndRevokedFalse(UUID userId) {
        return jpaRepository.findByUserIdAndRevokedFalse(userId);
    }
}
