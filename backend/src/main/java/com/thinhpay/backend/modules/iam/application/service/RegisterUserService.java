package com.thinhpay.backend.modules.iam.application.service;

import com.thinhpay.backend.modules.iam.application.dto.request.RegisterRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.RegisterResponse;
import com.thinhpay.backend.modules.iam.application.port.in.RegisterUseCase;
import com.thinhpay.backend.modules.iam.application.port.out.OtpCodeRepository;
import com.thinhpay.backend.modules.iam.application.port.out.UserRepository;
import com.thinhpay.backend.modules.iam.domain.otp.IamOtpCode;
import com.thinhpay.backend.modules.iam.domain.otp.OtpPurpose;
import com.thinhpay.backend.modules.iam.domain.otp.OtpType;
import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import com.thinhpay.backend.modules.iam.infrastructure.service.EmailService;
import com.thinhpay.backend.shared.exception.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RegisterUserService implements RegisterUseCase {

    UserRepository userRepository;
    OtpCodeRepository otpCodeRepository;
    PasswordEncoder passwordEncoder;
    EmailService emailService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw ValidationException.emailAlreadyExists(request.getEmail());
        }

        IamUser user = IamUser.createNew(request.getEmail(), request.getPhoneNumber(),
                passwordEncoder.encode(request.getPassword()), request.getFullName());

        IamUser savedUser = userRepository.save(user);
        // 3. Generate OTP cho email verification
        IamOtpCode otpCode = IamOtpCode.generate(
            savedUser.getId(),
            OtpType.EMAIL,
            OtpPurpose.VERIFY_EMAIL
        );
        otpCodeRepository.save(otpCode);

        // 4. Gửi OTP qua email
        emailService.sendVerificationOtp(savedUser.getEmailValue(), otpCode.getCode(), savedUser.getFullName());

        // 5. Return response with factory method
        return RegisterResponse.from(savedUser, otpCode);
    }
}
