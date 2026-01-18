package com.thinhpay.backend.modules.iam.application.dto.request;

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
public class UpdateProfileRequest {

    UUID userId;

    @Size(min = 2, max = 255, message = "Họ tên từ 2-255 ký tự")
    String fullName;

    @Size(max = 500, message = "Avatar URL tối đa 500 ký tự")
    String avatarUrl;
}

