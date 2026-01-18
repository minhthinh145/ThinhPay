package com.thinhpay.backend.modules.iam.infrastructure.persistence.adapter;

import com.thinhpay.backend.modules.iam.application.port.out.TokenBlacklistRepository;
import com.thinhpay.backend.modules.iam.domain.token.IamTokenBlacklist;
import com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa.IamTokenBlacklistRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implement TokenBlacklistRepository port.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenBlacklistRepositoryAdapter implements TokenBlacklistRepository {

    IamTokenBlacklistRepository jpaRepository;

    @Override
    public IamTokenBlacklist save(IamTokenBlacklist tokenBlacklist) {
        return jpaRepository.save(tokenBlacklist);
    }

    @Override
    public Optional<IamTokenBlacklist> findByJti(String jti) {
        return jpaRepository.findByJti(jti);
    }

    @Override
    public boolean existsByJti(String jti) {
        return jpaRepository.existsByJti(jti);
    }
}
