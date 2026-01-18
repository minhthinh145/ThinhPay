package com.thinhpay.backend.modules.iam.application.port.in;

import com.thinhpay.backend.modules.iam.application.dto.request.LoginRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.LoginResponse;

public interface LoginUseCase {
    LoginResponse login(LoginRequest request);
}
