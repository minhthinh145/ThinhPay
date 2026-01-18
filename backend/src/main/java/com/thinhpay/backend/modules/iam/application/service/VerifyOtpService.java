package com.thinhpay.backend.modules.iam.application.service;

import com.thinhpay.backend.modules.iam.application.dto.request.VerifyOtpRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.TokenResponse;
import com.thinhpay.backend.modules.iam.application.port.in.VerifyOtpUseCase;
import com.thinhpay.backend.modules.iam.application.port.out.OtpCodeRepository;
import com.thinhpay.backend.modules.iam.application.port.out.UserRepository;
import com.thinhpay.backend.modules.iam.domain.otp.IamOtpCode;
import com.thinhpay.backend.modules.iam.domain.otp.OtpPurpose;
import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import com.thinhpay.backend.shared.exception.ResourceNotFoundException;
import com.thinhpay.backend.shared.exception.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerifyOtpService implements VerifyOtpUseCase {

    OtpCodeRepository otpCodeRepository;
    UserRepository userRepository;

    @Override
    @Transactional
    public TokenResponse verifyOtp(VerifyOtpRequest request) {
        // 1. Tìm OTP code
        IamOtpCode otpCode = otpCodeRepository
            .findByUserIdAndPurposeAndVerifiedFalse(request.getUserId(), request.getPurpose())
            .orElseThrow(() -> new ValidationException("Không tìm thấy mã OTP"));

        // 2. Check expired
        if (otpCode.isExpired()) {
            throw ValidationException.otpExpired();
        }

        // 3. Check attempts
        if (otpCode.getAttempts() >= 3) {
            throw ValidationException.otpMaxAttemptsExceeded();
        }

        // 4. Verify code
        boolean isValid = otpCode.verify(request.getCode());
        if (!isValid) {
            otpCodeRepository.save(otpCode); // Save increased attempts
            throw ValidationException.invalidOtp();
        }

        // 5. Mark verified
        otpCodeRepository.save(otpCode);

        // 6. Update user status based on OTP purpose
        IamUser user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId().toString()));

        if (request.getPurpose() == OtpPurpose.VERIFY_EMAIL) {
            user.verifyEmail();
            userRepository.save(user);
        } else if (request.getPurpose() == OtpPurpose.VERIFY_PHONE) {
            user.verifyPhone();
            userRepository.save(user);
        }

        // 7. TODO: Generate JWT tokens
        String accessToken = "JWT_ACCESS_TOKEN_TODO"; // JwtTokenProvider.generateAccessToken(user)
        String refreshToken = "JWT_REFRESH_TOKEN_TODO"; // Generate refresh token

        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(900L)
            .build();
    }
}
