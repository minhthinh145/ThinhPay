package com.thinhpay.backend.modules.iam.infrastructure.persistence.adapter;

import com.thinhpay.backend.modules.iam.application.port.out.SessionRepository;
import com.thinhpay.backend.modules.iam.domain.session.IamSession;
import com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa.IamSessionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implement SessionRepository port.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionRepositoryAdapter implements SessionRepository {

    IamSessionRepository jpaRepository;

    @Override
    public IamSession save(IamSession session) {
        return jpaRepository.save(session);
    }

    @Override
    public Optional<IamSession> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<IamSession> findByUserIdAndActiveTrue(UUID userId) {
        return jpaRepository.findByUserIdAndActiveTrue(userId);
    }

    @Override
    public Optional<IamSession> findBySessionToken(String sessionToken) {
        return jpaRepository.findBySessionToken(sessionToken);
    }
}
