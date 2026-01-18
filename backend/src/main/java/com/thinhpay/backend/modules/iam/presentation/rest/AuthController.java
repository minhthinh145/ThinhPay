package com.thinhpay.backend.modules.iam.presentation.rest;

import com.thinhpay.backend.modules.iam.application.dto.request.*;
import com.thinhpay.backend.modules.iam.application.dto.response.*;
import com.thinhpay.backend.modules.iam.application.port.in.*;
import com.thinhpay.backend.shared.presentation.ApiResponse;
    import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Authentication Controller - Public endpoints.
 * Handles: register, login, logout, refresh token, OTP verification.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Authentication", description = "Authentication APIs - Register, Login, OTP Verification, Token Management")
public class    AuthController {

    RegisterUseCase registerUseCase;
    LoginUseCase loginUseCase;
    LogoutUseCase logoutUseCase;
    RefreshTokenUseCase refreshTokenUseCase;
    GenerateOtpUseCase generateOtpUseCase;
    VerifyOtpUseCase verifyOtpUseCase;

    /**
     * POST /api/v1/auth/register
     * Register new user account.
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Create a new user account. An OTP will be sent to the provided email for verification."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Registration successful. OTP sent to email.",
            content = @Content(schema = @Schema(implementation = RegisterResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input (duplicate email, weak password, etc.)"
        )
    })
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for email: {}", request.getEmail());
        RegisterResponse response = registerUseCase.register(request);
        return ApiResponse.success(response, "Registration successful. Please check your email for OTP.");
    }

    /**
     * POST /api/v1/auth/login
     * Login with email and password.
     * Returns JWT tokens or OTP_REQUIRED if 2FA is enabled.
     */
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate with email and password. Returns JWT access/refresh tokens or OTP_REQUIRED status if 2FA is enabled."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful or OTP required",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid credentials"
        )
    })
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        LoginResponse response = loginUseCase.login(request);

        if ("OTP_REQUIRED".equals(response.getStatus())) {
            return ApiResponse.success(response, "OTP has been sent to your email. Please verify to continue.");
        }

        return ApiResponse.success(response, "Login successful");
    }

    /**
     * POST /api/v1/auth/logout
     * Logout current session.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
        @RequestParam UUID userId,
        @RequestParam UUID sessionId
    ) {
        log.info("Logout request - userId: {}, sessionId: {}", userId, sessionId);
        logoutUseCase.logout(userId, sessionId);
        return ApiResponse.success(null, "Logout successful");
    }

    /**
     * POST /api/v1/auth/logout-all
     * Logout all sessions (all devices).
     */
    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(@RequestParam UUID userId) {
        log.info("Logout all sessions - userId: {}", userId);
        logoutUseCase.logoutAll(userId);
        return ApiResponse.success(null, "Logged out from all devices");
    }

    /**
     * POST /api/v1/auth/refresh
     * Refresh access token using refresh token.
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Get a new access token using refresh token. Implements token rotation for security."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token"
        )
    })
    public ApiResponse<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request");
        TokenResponse response = refreshTokenUseCase.refresh(request);
        return ApiResponse.success(response, "Token refreshed successfully");
    }

    /**
     * POST /api/v1/auth/verify-otp
     * Verify OTP code.
     */
    @PostMapping("/verify-otp")
    @Operation(
        summary = "Verify OTP code",
        description = "Verify the OTP code sent to email/SMS. Returns JWT tokens upon successful verification."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "OTP verified successfully",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid or expired OTP code"
        )
    })
    public ApiResponse<TokenResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("Verify OTP - userId: {}, purpose: {}", request.getUserId(), request.getPurpose());
        TokenResponse response = verifyOtpUseCase.verifyOtp(request);
        return ApiResponse.success(response, "OTP verified successfully");
    }

    /**
     * POST /api/v1/auth/generate-otp
     * Generate new OTP code.
     */
    @PostMapping("/generate-otp")
    public ApiResponse<OtpResponse> generateOtp(@Valid @RequestBody GenerateOtpRequest request) {
        log.info("Generate OTP - userId: {}, type: {}, purpose: {}",
            request.getUserId(), request.getType(), request.getPurpose());
        OtpResponse response = generateOtpUseCase.generate(request);
        return ApiResponse.success(response, "OTP has been sent");
    }

    /**
     * POST /api/v1/auth/resend-otp
     * Resend OTP (convenience endpoint - same as generate-otp).
     */
    @PostMapping("/resend-otp")
    public ApiResponse<OtpResponse> resendOtp(@Valid @RequestBody GenerateOtpRequest request) {
        log.info("Resend OTP - userId: {}, type: {}", request.getUserId(), request.getType());
        OtpResponse response = generateOtpUseCase.generate(request);
        return ApiResponse.success(response, "OTP has been resent");
    }
}
