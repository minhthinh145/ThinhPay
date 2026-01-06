package com.thinhpay.backend.shared.presentation;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonPropertyOrder({"success", "status", "message", "data", "timestamp"})
public class ApiResponse<T> {
    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();

    int status;
    String message;
    T data;
    String error;

    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }
    // -- Factory Methods -- //
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .status(201)
                .message("Created successfully")
                .data(data)
                .build();
    }

    public static ApiResponse<Void> failure(int status, String message, String errorDetail) {
        return ApiResponse.<Void>builder()
                .status(status)
                .message(message)
                .error(errorDetail)
                .build();
    }
}
