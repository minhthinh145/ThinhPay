package com.thinhpay.backend.modules.corebanking.application.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAccountRequest {
    @NotNull(message = "User ID must be required")
    UUID userId;

    @NotBlank(message = "Currency code is required")
    String currencyCode;
}
