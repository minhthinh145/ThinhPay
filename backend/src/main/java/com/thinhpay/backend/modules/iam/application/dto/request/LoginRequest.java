package com.thinhpay.backend.modules.iam.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class LoginRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, max = 255, message = "Password phải từ 8-255 ký tự")
    String password;

    String ipAddress;
    String userAgent;
    String deviceId;

    @Builder.Default
    Boolean rememberMe = false;
}

