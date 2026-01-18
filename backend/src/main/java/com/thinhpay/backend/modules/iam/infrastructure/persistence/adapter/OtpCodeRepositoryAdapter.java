package com.thinhpay.backend.modules.iam.infrastructure.persistence.adapter;

import com.thinhpay.backend.modules.iam.application.port.out.OtpCodeRepository;
import com.thinhpay.backend.modules.iam.domain.otp.IamOtpCode;
import com.thinhpay.backend.modules.iam.domain.otp.OtpPurpose;
import com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa.IamOtpCodeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implement OtpCodeRepository port.
 * Wrap JPA repository để tách biệt infrastructure khỏi application layer.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpCodeRepositoryAdapter implements OtpCodeRepository {

    IamOtpCodeRepository jpaRepository;

    @Override
    public IamOtpCode save(IamOtpCode otpCode) {
        return jpaRepository.save(otpCode);
    }

    @Override
    public Optional<IamOtpCode> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<IamOtpCode> findByUserIdAndPurposeAndVerifiedFalse(UUID userId, OtpPurpose purpose) {
        // JPA trả về List, lấy first element (latest based on createdAt)
        return jpaRepository.findFirstByUserIdAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(userId, purpose);
    }

    @Override
    public void deleteByExpiresAtBefore(Instant now) {
        jpaRepository.deleteByExpiresAtBefore(now);
    }
}
