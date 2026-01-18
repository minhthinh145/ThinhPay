package com.thinhpay.backend.modules.corebanking.application.port.in;

import com.thinhpay.backend.modules.corebanking.application.dto.request.WithdrawRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.response.AccountResponse;

public interface WithdrawUseCase {
    AccountResponse withdraw(WithdrawRequest request);
}
