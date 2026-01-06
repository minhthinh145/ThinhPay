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

    /**
     * Checks whether the response status represents a successful HTTP status code.
     *
     * @return `true` if the status is between 200 and 299 inclusive, `false` otherwise.
     */
    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }
    /**
     * Create a successful API response with the given data and message.
     *
     * @param data    the payload to include in the response; may be null
     * @param message a human-readable message describing the response
     * @param <T>     the type of the response payload
     * @return        an ApiResponse with status 200, the provided message, and the provided data
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Create a successful API response containing the given payload and a default message.
     *
     * @param data the response payload; may be null
     * @return an ApiResponse with status 200, message "Success", and the provided data
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }

    /**
     * Create an ApiResponse with HTTP status 201 and a "Created successfully" message containing the provided payload.
     *
     * @param data the response payload to include; may be null
     * @param <T> the type of the payload
     * @return an ApiResponse with status 201, message "Created successfully", and the given data
     */
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .status(201)
                .message("Created successfully")
                .data(data)
                .build();
    }

    /**
     * Create a failure ApiResponse with the given status, message, and error detail.
     *
     * @param status      HTTP-like status code representing the failure
     * @param message     human-readable message describing the failure
     * @param errorDetail optional detailed error information
     * @return            an ApiResponse with no data where `status`, `message`, and `error` are set as provided
     */
    public static ApiResponse<Void> failure(int status, String message, String errorDetail) {
        return ApiResponse.<Void>builder()
                .status(status)
                .message(message)
                .error(errorDetail)
                .build();
    }
}