package com.thinhpay.backend.modules.iam.application.port.in;

import com.thinhpay.backend.modules.iam.application.dto.request.VerifyOtpRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.TokenResponse;

public interface VerifyOtpUseCase {
    TokenResponse verifyOtp(VerifyOtpRequest request);
}
