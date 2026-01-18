package com.thinhpay.backend.shared.presentation;

import com.thinhpay.backend.shared.exception.DomainException;
import com.thinhpay.backend.shared.exception.ResourceNotFoundException;
// Bỏ import ErrorResponse cũ
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Domain Exception -> 400
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        log.warn("Domain Exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.failure(400, ex.getMessage(), ex.getErrorCode())
        );
    }

    // 2. Not Found -> 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Not Found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.failure(404, ex.getMessage(), "RESOURCE_NOT_FOUND")
        );
    }

    // 3. Validation -> 400
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

    // 3b. Unsupported Media Type -> 415
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        log.warn("Unsupported Media Type: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(
                ApiResponse.failure(415, "Unsupported Media Type", ex.getContentType() != null ? ex.getContentType().toString() : "unknown")
        );
    }

    // 4. Entity Logic Errors -> 400
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleStateError(RuntimeException ex) {
        log.warn("Logic Error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.failure(400, ex.getMessage(), "INVALID_STATE")
        );
    }

    // 5. System Error -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
        log.error("System Error", ex);
        return ResponseEntity.internalServerError().body(
                ApiResponse.failure(500, "Internal Server Error", ex.getMessage())
        );
    }
}