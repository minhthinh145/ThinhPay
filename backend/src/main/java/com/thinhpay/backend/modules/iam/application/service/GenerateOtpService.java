package com.thinhpay.backend.modules.iam.application.service;

import com.thinhpay.backend.modules.iam.application.dto.request.GenerateOtpRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.OtpResponse;
import com.thinhpay.backend.modules.iam.application.port.in.GenerateOtpUseCase;
import com.thinhpay.backend.modules.iam.application.port.out.OtpCodeRepository;
import com.thinhpay.backend.modules.iam.application.port.out.UserRepository;
import com.thinhpay.backend.modules.iam.domain.otp.IamOtpCode;
import com.thinhpay.backend.modules.iam.domain.otp.OtpType;
import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import com.thinhpay.backend.modules.iam.infrastructure.service.EmailService;
import com.thinhpay.backend.shared.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenerateOtpService implements GenerateOtpUseCase {

    OtpCodeRepository otpCodeRepository;
    UserRepository userRepository;
    EmailService emailService;

    @Override
    @Transactional
    public OtpResponse generate(GenerateOtpRequest request) {
        // 1. Get user info for masking
        IamUser user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId().toString()));

        // 2. Check xem đã có OTP chưa expired chưa
        Optional<IamOtpCode> existingOtp = otpCodeRepository
            .findByUserIdAndPurposeAndVerifiedFalse(request.getUserId(), request.getPurpose());

        if (existingOtp.isPresent() && !existingOtp.get().isExpired()) {
            IamOtpCode otp = existingOtp.get();
            long secondsRemaining = Duration.between(Instant.now(), otp.getExpiresAt()).getSeconds();

            return OtpResponse.builder()
                .status("ALREADY_EXISTS")
                .otpType(otp.getType().name())
                .maskedDestination(maskDestination(user, otp.getType()))
                .message("Mã OTP trước đó vẫn còn hiệu lực")
                .remainingAttempts(3 - otp.getAttempts())
                .expiresIn(secondsRemaining)
                .build();
        }

        // 3. Generate OTP mới
        IamOtpCode otpCode = IamOtpCode.generate(
            request.getUserId(),
            request.getType(),
            request.getPurpose()
        );
        otpCodeRepository.save(otpCode);

        // 4. Gửi OTP qua email/SMS
        if (request.getType() == OtpType.EMAIL) {
            emailService.sendVerificationOtp(user.getEmailValue(), otpCode.getCode(), user.getFullName());
        }
        // TODO: SMS service integration
        // else {
        //     smsService.sendOtp(user.getPhoneValue(), otpCode.getCode());
        // }

        return OtpResponse.builder()
            .status("SENT")
            .otpType(otpCode.getType().name())
            .maskedDestination(maskDestination(user, otpCode.getType()))
            .message("Mã OTP đã được gửi")
            .remainingAttempts(3)
            .expiresIn(300L) // 5 minutes
            .build();
    }

    private String maskDestination(IamUser user, OtpType type) {
        if (type == OtpType.EMAIL) {
            return maskEmail(user.getEmailValue());
        } else {
            return maskPhone(user.getPhoneValue());
        }
    }

    private String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) return email;
        String localPart = parts[0];
        if (localPart.length() <= 2) return email;
        return localPart.charAt(0) + "***@" + parts[1];
    }

    private String maskPhone(String phone) {
        if (phone.length() <= 4) return phone;
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 3);
    }
}
