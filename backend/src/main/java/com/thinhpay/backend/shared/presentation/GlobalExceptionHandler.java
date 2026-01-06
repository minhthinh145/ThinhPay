package com.thinhpay.backend.shared.presentation;

import com.thinhpay.backend.shared.domain.DomainException;
import com.thinhpay.backend.shared.domain.ResourceNotFoundException;
// Bỏ import ErrorResponse cũ
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles DomainException by producing a 400 Bad Request response.
     *
     * @param ex the DomainException whose message and error code will be used in the response
     * @return a ResponseEntity with HTTP 400 and an ApiResponse indicating failure; the ApiResponse contains the exception message and the exception's error code
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        log.warn("Domain Exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.failure(400, ex.getMessage(), ex.getErrorCode())
        );
    }

    /**
     * Converts a ResourceNotFoundException into a 404 ApiResponse payload.
     *
     * @param ex the caught ResourceNotFoundException
     * @return a ResponseEntity containing an ApiResponse failure with HTTP 404, the exception message, and error code "RESOURCE_NOT_FOUND"
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Not Found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.failure(404, ex.getMessage(), "RESOURCE_NOT_FOUND")
        );
    }

    /**
     * Handles validation failures from method argument binding and responds with a 400 ApiResponse containing aggregated field error details.
     *
     * @param ex the MethodArgumentNotValidException containing binding/validation errors
     * @return a ResponseEntity wrapping an ApiResponse<Void> with failure code 400, message "Validation Error", and a comma-separated list of field errors formatted as "field: message" as details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation: {}", details);
        return ResponseEntity.badRequest().body(
                ApiResponse.failure(400, "Validation Error", details)
        );
    }

    /**
     * Maps IllegalArgumentException and IllegalStateException to an HTTP 400 response with a standardized ApiResponse.
     *
     * @param ex the thrown RuntimeException (IllegalArgumentException or IllegalStateException); its message is used as the response message
     * @return a ResponseEntity containing an ApiResponse<Void> with status code 400, the exception message, and error code "INVALID_STATE"
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleStateError(RuntimeException ex) {
        log.warn("Logic Error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.failure(400, ex.getMessage(), "INVALID_STATE")
        );
    }

    /**
     * Handles uncaught exceptions and maps them to an HTTP 500 Internal Server Error response.
     *
     * @param ex the uncaught exception that triggered this handler
     * @return a ResponseEntity containing an ApiResponse<Void> with failure code 500, message "Internal Server Error", and the exception message as additional detail
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
        log.error("System Error", ex);
        return ResponseEntity.internalServerError().body(
                ApiResponse.failure(500, "Internal Server Error", ex.getMessage())
        );
    }
}