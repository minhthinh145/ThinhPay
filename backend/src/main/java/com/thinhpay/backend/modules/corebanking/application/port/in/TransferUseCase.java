package com.thinhpay.backend.modules.corebanking.application.port.in;

import com.thinhpay.backend.modules.corebanking.application.dto.request.TransferRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.response.TransferResponse;

public interface TransferUseCase {
    TransferResponse transfer(TransferRequest request);
}
