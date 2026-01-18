package com.thinhpay.backend.modules.iam.infrastructure.persistence.mapper;

import com.thinhpay.backend.modules.iam.application.dto.response.OtpResponse;
import com.thinhpay.backend.modules.iam.domain.otp.IamOtpCode;

import java.time.Duration;
import java.time.Instant;

public class OtpMapper {

    public OtpResponse toResponse(IamOtpCode otpCode, String status, String message){
        long expiresIn = Duration.between(Instant.now(), otpCode.getExpiresAt()).getSeconds();

        return OtpResponse.builder()
                .status(status)
                .otpType(otpCode.getType().name())
                .maskedDestination(maskDestination(otpCode))
                .message(message)
                .remainingAttempts(3 - otpCode.getAttempts())
                .expiresIn(Math.max(0, expiresIn))
                .build();
    }

    private String maskDestination(IamOtpCode otpCode) {
        // TODO: Get actual email/phone from user
        return "***";
    }
}

