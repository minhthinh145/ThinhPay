package com.thinhpay.backend.modules.iam.application.dto.response;

import com.thinhpay.backend.modules.iam.domain.otp.IamOtpCode;
import com.thinhpay.backend.modules.iam.domain.token.IamRefreshToken;
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
public class LoginResponse {

    UUID userId;
    String status; // SUCCESS, OTP_REQUIRED, ACCOUNT_LOCKED

    // Tokens (if SUCCESS)
    String accessToken;
    String refreshToken;
    Long expiresIn; // seconds

    // OTP info (if OTP_REQUIRED)
    String otpType; // EMAIL, SMS
    String maskedDestination; // "a***@gmail.com", "09****123"

    public static LoginResponse success(IamUser user, String accessToken, IamRefreshToken refreshToken) {
        return LoginResponse.builder()
                .userId(user.getId())
                .status("SUCCESS")
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(900L) // 15 minutes
                .build();
    }

    public static LoginResponse otpRequired(IamUser user, IamOtpCode otpCode) {
        return LoginResponse.builder()
                .userId(user.getId())
                .status("OTP_REQUIRED")
                .otpType(otpCode.getType().name())
                .maskedDestination(maskEmail(user.getEmailValue()))
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

