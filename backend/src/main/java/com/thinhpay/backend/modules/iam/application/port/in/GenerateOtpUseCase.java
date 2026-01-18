package com.thinhpay.backend.modules.iam.application.port.in;

import com.thinhpay.backend.modules.iam.application.dto.request.GenerateOtpRequest;
import com.thinhpay.backend.modules.iam.application.dto.response.OtpResponse;

public interface GenerateOtpUseCase {
    OtpResponse generate(GenerateOtpRequest request);
}
