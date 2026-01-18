package com.thinhpay.backend.modules.iam.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinhpay.backend.modules.iam.application.dto.request.RegisterRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.RegisterResponse;
import com.thinhpay.backend.modules.iam.application.port.in.RegisterUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests cho AuthController.
 * Sử dụng MockMvc để test REST API.
 *
 * Note: Exclude auto-configurations để tránh load JPA, Security
 * vì đây là unit test cho controller logic thuần túy.
 */
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        // Exclude Security (không test security logic ở đây)
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
        // Exclude JPA (không cần database)
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        // Exclude Redis (không cần Redis)
        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class
    },
    excludeFilters = {
        @org.springframework.context.annotation.ComponentScan.Filter(
            type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
            classes = {
                com.thinhpay.backend.shared.infrastructure.security.JwtAuthenticationFilter.class,
                com.thinhpay.backend.shared.infrastructure.security.JwtTokenProvider.class,
                com.thinhpay.backend.BackendApplication.class
            }
        )
    }
)
@Import(TestSecurityConfig.class)  // Import test security config
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController API Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUseCase registerUseCase;

    // Mock other use cases (needed because AuthController might inject them)
    @MockitoBean
    private com.thinhpay.backend.modules.iam.application.port.in.LoginUseCase loginUseCase;

    @MockitoBean
    private com.thinhpay.backend.modules.iam.application.port.in.LogoutUseCase logoutUseCase;

    @MockitoBean
    private com.thinhpay.backend.modules.iam.application.port.in.RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private com.thinhpay.backend.modules.iam.application.port.in.GenerateOtpUseCase generateOtpUseCase;

    @MockitoBean
    private com.thinhpay.backend.modules.iam.application.port.in.VerifyOtpUseCase verifyOtpUseCase;

    // Mock security beans to prevent context loading issues
    @MockitoBean
    private com.thinhpay.backend.shared.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private com.thinhpay.backend.shared.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;


    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("Test123!@#");
        validRegisterRequest.setFullName("Test User");
        validRegisterRequest.setPhoneNumber("0901234567");  // Vietnamese format
    }

    @Test
    @DisplayName("POST /auth/register should return 200 with RegisterResponse")
    void testRegisterEndpoint() throws Exception {
        // Given
        RegisterResponse mockResponse = RegisterResponse.builder()
            .userId(UUID.randomUUID())
            .email(validRegisterRequest.getEmail())
            .phoneNumber(validRegisterRequest.getPhoneNumber())
            .status("OTP_SENT")
            .otpType("EMAIL")
            .maskedDestination("t***@example.com")
            .message("Mã OTP đã được gửi đến email của bạn")
            .build();

        when(registerUseCase.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value(validRegisterRequest.getEmail()))
            .andExpect(jsonPath("$.data.status").value("OTP_SENT"))
            .andExpect(jsonPath("$.message").value("Registration successful. Please check your email for OTP."));
    }

    @Test
    @DisplayName("POST /auth/register should return 400 when email is invalid")
    void testRegisterWithInvalidEmail() throws Exception {
        // Given
        validRegisterRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register should return 400 when password is too short")
    void testRegisterWithShortPassword() throws Exception {
        // Given
        validRegisterRequest.setPassword("123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register should return 400 when required fields are missing")
    void testRegisterWithMissingFields() throws Exception {
        // Given
        RegisterRequest incompleteRequest = new RegisterRequest();
        incompleteRequest.setEmail("test@example.com");
        // Missing password, fullName, phoneNumber

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register should return 415 when content type is not JSON")
    void testRegisterWithWrongContentType() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
            .andDo(print())
            .andExpect(status().isUnsupportedMediaType());
    }
}
