package com.thinhpay.backend.modules.iam.application.dto.request;

import com.thinhpay.backend.modules.iam.domain.otp.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class VerifyOtpRequest {

    @NotNull(message = "User ID không được null")
    UUID userId;

    @NotBlank(message = "OTP code không được để trống")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP phải là 6 chữ số")
    String code;

    @NotNull(message = "Purpose không được null")
    OtpPurpose purpose;

    // For LOGIN purpose
    String ipAddress;
    String userAgent;
    String deviceId;
}

