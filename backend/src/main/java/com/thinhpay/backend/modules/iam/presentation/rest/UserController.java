package com.thinhpay.backend.modules.iam.presentation.rest;

import com.thinhpay.backend.modules.iam.application.dto.request.ChangePasswordRequest;
import com.thinhpay.backend.modules.iam.application.dto.request.UpdateProfileRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.SessionResponse;
import com.thinhpay.backend.modules.iam.application.dto.response.UserProfileResponse;
import com.thinhpay.backend.modules.iam.application.port.in.ChangePasswordUseCase;
import com.thinhpay.backend.modules.iam.application.port.in.SessionManagementUseCase;
import com.thinhpay.backend.modules.iam.application.port.in.UserProfileUseCase;
import com.thinhpay.backend.shared.presentation.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * User Controller - Authenticated endpoints.
 * Handles: profile management, password change, session management.
 * Requires JWT authentication.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    UserProfileUseCase userProfileUseCase;
    ChangePasswordUseCase changePasswordUseCase;
    SessionManagementUseCase sessionManagementUseCase;

    /**
     * GET /api/v1/users/me
     * Get current user profile.
     * Uses Authentication object from SecurityContext (set by JwtAuthenticationFilter).
     */
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getCurrentUserProfile(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        log.info("Get profile - userId: {}", userId);

        UserProfileResponse response = userProfileUseCase.getUserProfile(userId);
        return ApiResponse.success(response);
    }

    /**
     * GET /api/v1/users/{userId}
     * Get user profile by ID.
     * (Can be used by admin or self)
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getUserProfile(@PathVariable UUID userId) {
        log.info("Get profile by ID - userId: {}", userId);
        UserProfileResponse response = userProfileUseCase.getUserProfile(userId);
        return ApiResponse.success(response);
    }

    /**
     * PUT /api/v1/users/me
     * Update current user profile.
     */
    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateCurrentUserProfile(
        Authentication authentication,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        log.info("Update profile - userId: {}", userId);

        // Set userId from authentication
        request.setUserId(userId);

        UserProfileResponse response = userProfileUseCase.updateProfile(request);
        return ApiResponse.success(response, "Profile updated successfully");
    }

    /**
     * POST /api/v1/users/change-password
     * Change user password.
     */
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
        Authentication authentication,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        log.info("Change password - userId: {}", userId);

        // Set userId from authentication
        request.setUserId(userId);

        changePasswordUseCase.changePassword(request);
        return ApiResponse.success(null, "Password changed successfully");
    }

    /**
     * GET /api/v1/users/sessions
     * Get all active sessions of current user.
     */
    @GetMapping("/sessions")
    public ApiResponse<List<SessionResponse>> getActiveSessions(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        log.info("Get active sessions - userId: {}", userId);

        List<SessionResponse> sessions = sessionManagementUseCase.getActiveSessions(userId);
        return ApiResponse.success(sessions);
    }

    /**
     * DELETE /api/v1/users/sessions/{sessionId}
     * Terminate a specific session.
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> logoutSession(
        Authentication authentication,
        @PathVariable UUID sessionId
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        log.info("Terminate session - userId: {}, sessionId: {}", userId, sessionId);

        sessionManagementUseCase.terminateSession(userId, sessionId);
        return ApiResponse.success(null, "Session terminated successfully");
    }
}
