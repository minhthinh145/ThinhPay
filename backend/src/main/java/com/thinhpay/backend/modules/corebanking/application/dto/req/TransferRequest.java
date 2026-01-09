package com.thinhpay.backend.modules.corebanking.application.dto.req;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferRequest {
    @NotNull(message = "Request ID must be required")
    String requestId;

    @NotNull(message = "Sender User ID must be required")
    UUID senderUserId;

    @NotNull(message = "Receiver user ID must be required")
    UUID receiverUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @DecimalMax(value = "1000000000", message = "Amount exceeds maximum limit")
    BigDecimal amount;

    @NotBlank(message = "Currency is required")
    String currency;

    @Size(max = 500)
    String description;

    //helper
    public String getFromCurrency() {
        return currency;
    }

    public String getToCurrency() {
        return currency; // will be update later
        //waring: for future use only
    }
}
