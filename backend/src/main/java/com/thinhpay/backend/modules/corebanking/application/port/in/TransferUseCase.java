package com.thinhpay.backend.modules.corebanking.application.port.in;

import com.thinhpay.backend.modules.corebanking.application.dto.req.TransferRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.res.TransferResponse;

public interface TransferUseCase {
    TransferResponse transfer(TransferRequest request);
}
