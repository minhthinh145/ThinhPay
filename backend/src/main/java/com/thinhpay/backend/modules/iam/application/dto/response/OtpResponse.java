package com.thinhpay.backend.modules.iam.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class OtpResponse {

    String status; // SENT, ALREADY_EXISTS, RATE_LIMITED
    String otpType; // EMAIL, SMS
    String maskedDestination;
    String message;
    Integer remainingAttempts;
    Long expiresIn; // seconds
}

