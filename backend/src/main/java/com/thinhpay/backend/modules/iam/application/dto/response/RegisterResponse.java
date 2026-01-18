package com.thinhpay.backend.modules.iam.application.dto.response;

import com.thinhpay.backend.modules.iam.domain.otp.IamOtpCode;
import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class RegisterResponse {

    UUID userId;
    String email;
    String phoneNumber;
    String status; // OTP_SENT, PENDING_VERIFICATION

    // OTP info
    String otpType; // EMAIL, SMS
    String maskedDestination;
    String message;

    public static RegisterResponse from(IamUser user, IamOtpCode otpCode) {
        return RegisterResponse.builder()
                .userId(user.getId())
                .email(user.getEmailValue())
                .phoneNumber(user.getPhoneValue())
                .status("OTP_SENT")
                .otpType(otpCode.getType().name())
                .maskedDestination(maskEmail(user.getEmailValue()))
                .message("Mã OTP đã được gửi đến email của bạn")
                .build();
    }

    private static String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) return email;
        String localPart = parts[0];
        if (localPart.length() <= 2) return email;
        return localPart.charAt(0) + "***@" + parts[1];
    }
}

