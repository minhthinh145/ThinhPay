package com.thinhpay.backend.modules.corebanking.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WithdrawRequest {
    @NotNull
    UUID userId;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Số tiền rút phải lớn hơn 0")
    BigDecimal amount;

    @NotBlank
    String requestId;
}