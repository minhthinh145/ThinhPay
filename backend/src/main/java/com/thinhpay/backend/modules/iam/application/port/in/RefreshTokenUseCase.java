package com.thinhpay.backend.modules.iam.application.port.in;

import com.thinhpay.backend.modules.iam.application.dto.request.RefreshTokenRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.TokenResponse;

public interface RefreshTokenUseCase {
    TokenResponse refresh(RefreshTokenRequest request);
}
