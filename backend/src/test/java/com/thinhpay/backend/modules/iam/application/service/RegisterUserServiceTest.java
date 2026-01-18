package com.thinhpay.backend.modules.iam.application.service;

import com.thinhpay.backend.modules.iam.application.dto.request.RegisterRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.RegisterResponse;
import com.thinhpay.backend.modules.iam.application.port.out.OtpCodeRepository;
import com.thinhpay.backend.modules.iam.application.port.out.UserRepository;
import com.thinhpay.backend.modules.iam.domain.otp.IamOtpCode;
import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import com.thinhpay.backend.modules.iam.infrastructure.service.EmailService;
import com.thinhpay.backend.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho RegisterUserService.
 * Sử dụng Mockito để mock dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserService Tests")
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RegisterUserService registerUserService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("Test123!@#");
        validRequest.setFullName("Test User");
        validRequest.setPhoneNumber("+84901234567");
    }

    @Test
    @DisplayName("Should register user successfully")
    void testRegisterSuccess() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(IamUser.class))).thenAnswer(invocation -> {
            IamUser user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            return user;
        });
        when(otpCodeRepository.save(any(IamOtpCode.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendVerificationOtp(anyString(), anyString(), anyString());

        // When
        RegisterResponse response = registerUserService.register(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(validRequest.getEmail(), response.getEmail());
        assertEquals("OTP_SENT", response.getStatus());
        assertNotNull(response.getUserId());

        // Verify interactions
        verify(userRepository).existsByEmail(validRequest.getEmail());
        verify(passwordEncoder).encode(validRequest.getPassword());
        verify(userRepository).save(any(IamUser.class));
        verify(otpCodeRepository).save(any(IamOtpCode.class));
        verify(emailService).sendVerificationOtp(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegisterWithDuplicateEmail() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(ValidationException.class,
            () -> registerUserService.register(validRequest));

        verify(userRepository).existsByEmail(validRequest.getEmail());
        verify(userRepository, never()).save(any());
        verify(otpCodeRepository, never()).save(any());
        verify(emailService, never()).sendVerificationOtp(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should hash password before saving")
    void testPasswordHashing() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("super_secure_hash");
        when(userRepository.save(any(IamUser.class))).thenAnswer(invocation -> {
            IamUser user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            return user;
        });
        when(otpCodeRepository.save(any(IamOtpCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        registerUserService.register(validRequest);

        // Then
        ArgumentCaptor<IamUser> userCaptor = ArgumentCaptor.forClass(IamUser.class);
        verify(userRepository).save(userCaptor.capture());

        IamUser savedUser = userCaptor.getValue();
        assertEquals("super_secure_hash", savedUser.getPasswordHash());
        verify(passwordEncoder).encode(validRequest.getPassword());
    }

    @Test
    @DisplayName("Should create user with correct initial values")
    void testUserCreationWithCorrectValues() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(IamUser.class))).thenAnswer(invocation -> {
            IamUser user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            return user;
        });
        when(otpCodeRepository.save(any(IamOtpCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        registerUserService.register(validRequest);

        // Then
        ArgumentCaptor<IamUser> userCaptor = ArgumentCaptor.forClass(IamUser.class);
        verify(userRepository).save(userCaptor.capture());

        IamUser savedUser = userCaptor.getValue();
        assertEquals(validRequest.getEmail(), savedUser.getEmailValue());
        assertEquals(validRequest.getPhoneNumber(), savedUser.getPhoneValue());
        assertEquals(validRequest.getFullName(), savedUser.getFullName());
        assertEquals("USER", savedUser.getRoleId());
        assertFalse(savedUser.getEmailVerified());
        assertFalse(savedUser.getPhoneVerified());
    }

    @Test
    @DisplayName("Should generate OTP for email verification")
    void testOtpGeneration() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(IamUser.class))).thenAnswer(invocation -> {
            IamUser user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            return user;
        });
        when(otpCodeRepository.save(any(IamOtpCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        registerUserService.register(validRequest);

        // Then
        ArgumentCaptor<IamOtpCode> otpCaptor = ArgumentCaptor.forClass(IamOtpCode.class);
        verify(otpCodeRepository).save(otpCaptor.capture());

        IamOtpCode savedOtp = otpCaptor.getValue();
        assertNotNull(savedOtp.getCode());
        assertEquals(6, savedOtp.getCode().length());
        assertFalse(savedOtp.getVerified());
    }

    @Test
    @DisplayName("Should send verification email")
    void testEmailSending() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(IamUser.class))).thenAnswer(invocation -> {
            IamUser user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            return user;
        });
        when(otpCodeRepository.save(any(IamOtpCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        registerUserService.register(validRequest);

        // Then
        verify(emailService).sendVerificationOtp(
            eq(validRequest.getEmail()),
            anyString(), // OTP code
            eq(validRequest.getFullName())
        );
    }
}
