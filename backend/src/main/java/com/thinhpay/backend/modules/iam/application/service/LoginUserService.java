package com.thinhpay.backend.modules.iam.application.service;

import com.thinhpay.backend.modules.iam.application.dto.request.LoginRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.LoginResponse;
import com.thinhpay.backend.modules.iam.application.port.in.LoginUseCase;
import com.thinhpay.backend.modules.iam.application.port.out.OtpCodeRepository;
import com.thinhpay.backend.modules.iam.application.port.out.RefreshTokenRepository;
import com.thinhpay.backend.modules.iam.application.port.out.SessionRepository;
import com.thinhpay.backend.modules.iam.application.port.out.UserRepository;
import com.thinhpay.backend.modules.iam.domain.otp.IamOtpCode;
import com.thinhpay.backend.modules.iam.domain.otp.OtpPurpose;
import com.thinhpay.backend.modules.iam.domain.otp.OtpType;
import com.thinhpay.backend.modules.iam.domain.session.IamSession;
import com.thinhpay.backend.modules.iam.domain.token.IamRefreshToken;
import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import com.thinhpay.backend.shared.infrastructure.security.JwtTokenProvider;
import com.thinhpay.backend.modules.iam.infrastructure.service.EmailService;
import com.thinhpay.backend.shared.exception.AuthenticationException;
import com.thinhpay.backend.shared.exception.AuthorizationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginUserService implements LoginUseCase {

    UserRepository userRepository;
    SessionRepository sessionRepository;
    RefreshTokenRepository refreshTokenRepository;
    OtpCodeRepository otpCodeRepository;
    PasswordEncoder passwordEncoder;
    JwtTokenProvider jwtTokenProvider;
    EmailService emailService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. Tìm user theo email
        IamUser user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(AuthenticationException::invalidCredentials);

        // 2. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw AuthenticationException.invalidCredentials();
        }

        // 3. Check user có thể login không
        if (!user.canLogin()) {
            if (user.getStatus().name().equals("LOCKED")) {
                throw AuthorizationException.accountLocked();
            }
            if (user.getStatus().name().equals("SUSPENDED")) {
                throw AuthorizationException.accountSuspended();
            }
            throw new AuthenticationException("Tài khoản chưa được kích hoạt");
        }

        // 4. Nếu yêu cầu 2FA, generate OTP
        boolean require2FA = user.getEmailVerified() && user.getPhoneVerified();
        if (require2FA) {
            IamOtpCode otpCode = IamOtpCode.generate(
                user.getId(),
                OtpType.EMAIL,
                OtpPurpose.LOGIN
            );
            otpCodeRepository.save(otpCode);

            // Gửi OTP qua email
            emailService.sendLoginOtp(user.getEmailValue(), otpCode.getCode(), user.getFullName());

            return LoginResponse.otpRequired(user, otpCode);
        }

        // 5. Login thành công - Generate tokens
        String accessTokenJti = UUID.randomUUID().toString();

        // Create session
        IamSession session = IamSession.create(
            user.getId(),
            accessTokenJti,
            request.getIpAddress(),
            request.getUserAgent(),
            request.getDeviceId(),
            15 // 15 minutes
        );
        sessionRepository.save(session);

        // Create refresh token
        IamRefreshToken refreshToken = IamRefreshToken.create(
            user.getId(),
            UUID.randomUUID().toString(),
            request.getDeviceId(),
            7 // 7 days
        );
        refreshTokenRepository.save(refreshToken);

        // Generate real JWT access token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), accessTokenJti);

        return LoginResponse.success(user, accessToken, refreshToken);
    }
}
