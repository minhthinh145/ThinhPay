package com.thinhpay.backend.modules.iam.application.dto.request;

import com.thinhpay.backend.modules.iam.domain.otp.OtpPurpose;
import com.thinhpay.backend.modules.iam.domain.otp.OtpType;
import jakarta.validation.constraints.NotNull;
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
public class GenerateOtpRequest {

    @NotNull(message = "User ID không được null")
    UUID userId;

    @NotNull(message = "OTP type không được null")
    OtpType type;

    @NotNull(message = "Purpose không được null")
    OtpPurpose purpose;
}

