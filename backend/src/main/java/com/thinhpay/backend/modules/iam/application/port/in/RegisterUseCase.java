package com.thinhpay.backend.modules.iam.application.port.in;

import com.thinhpay.backend.modules.iam.application.dto.request.RegisterRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.RegisterResponse;

public interface RegisterUseCase {
    RegisterResponse register(RegisterRequest request);
}
