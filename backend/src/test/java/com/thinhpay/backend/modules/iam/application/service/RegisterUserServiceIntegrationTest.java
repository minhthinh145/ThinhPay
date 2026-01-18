package com.thinhpay.backend.modules.iam.application.service;

import com.thinhpay.backend.modules.iam.BaseIntegrationTest;
import com.thinhpay.backend.modules.iam.application.dto.request.RegisterRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.RegisterResponse;
import com.thinhpay.backend.modules.iam.application.port.in.RegisterUseCase;
import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import com.thinhpay.backend.modules.iam.domain.user.UserAccountStatus;
import com.thinhpay.backend.shared.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test cho RegisterUserService.
 * Follow pattern từ CoreBanking DepositUseCaseTest.
 */
@DisplayName("RegisterUserService Integration Tests")
class RegisterUserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RegisterUseCase registerUseCase;

    @Test
    @DisplayName("Should register user successfully and send OTP email")
    void should_RegisterSuccessfully() {
        // GIVEN
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("Test123!@#");
        request.setFullName("New Test User");
        request.setPhoneNumber("+84901234567");

        // WHEN
        RegisterResponse response = registerUseCase.register(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getPhoneNumber()).isEqualTo(request.getPhoneNumber());
        assertThat(response.getStatus()).isEqualTo("OTP_SENT");
        assertThat(response.getOtpType()).isEqualTo("EMAIL");
        assertThat(response.getUserId()).isNotNull();

        // Verify user được tạo trong database
        IamUser savedUser = userRepository.findById(response.getUserId()).orElseThrow();
        assertThat(savedUser.getEmailValue()).isEqualTo(request.getEmail());
        assertThat(savedUser.getFullName()).isEqualTo(request.getFullName());
        assertThat(savedUser.getStatus()).isEqualTo(UserAccountStatus.PENDING_VERIFICATION);
        assertThat(savedUser.getEmailVerified()).isFalse();
        assertThat(savedUser.getPhoneVerified()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void should_ThrowException_When_EmailExists() {
        // GIVEN
        String existingEmail = "existing@example.com";
        createTestUser(existingEmail, "Password123", "Existing User");

        RegisterRequest request = new RegisterRequest();
        request.setEmail(existingEmail);
        request.setPassword("Test123!@#");
        request.setFullName("Duplicate User");
        request.setPhoneNumber("+84987654321");

        // WHEN & THEN
        assertThrows(ValidationException.class, () -> {
            registerUseCase.register(request);
        });
    }

    @Test
    @DisplayName("Should hash password before saving")
    void should_HashPasswordBeforeSaving() {
        // GIVEN
        RegisterRequest request = new RegisterRequest();
        request.setEmail("secure@example.com");
        request.setPassword("PlainPassword123");
        request.setFullName("Secure User");
        request.setPhoneNumber("+84912345678");

        // WHEN
        RegisterResponse response = registerUseCase.register(request);

        // THEN
        IamUser savedUser = userRepository.findById(response.getUserId()).orElseThrow();

        // Password phải được hash (BCrypt)
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("PlainPassword123");
        assertThat(savedUser.getPasswordHash()).startsWith("$2"); // BCrypt prefix

        // Verify password matches
        assertThat(passwordEncoder.matches("PlainPassword123", savedUser.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("Should create user with default role USER")
    void should_CreateUserWithDefaultRole() {
        // GIVEN
        RegisterRequest request = new RegisterRequest();
        request.setEmail("roletest@example.com");
        request.setPassword("Test123!@#");
        request.setFullName("Role Test User");
        request.setPhoneNumber("+84923456789");

        // WHEN
        RegisterResponse response = registerUseCase.register(request);

        // THEN
        IamUser savedUser = userRepository.findById(response.getUserId()).orElseThrow();
        assertThat(savedUser.getRoleId()).isEqualTo("USER");
    }
}
