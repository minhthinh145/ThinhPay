package com.thinhpay.backend.modules.iam.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class ChangePasswordRequest {

    UUID userId;

    @NotBlank(message = "Password hiện tại không được để trống")
    String currentPassword;

    @NotBlank(message = "Password mới không được để trống")
    @Size(min = 8, max = 255, message = "Password phải từ 8-255 ký tự")
    String newPassword;
}

