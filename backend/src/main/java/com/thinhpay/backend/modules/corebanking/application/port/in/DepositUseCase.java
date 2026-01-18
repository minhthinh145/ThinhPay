package com.thinhpay.backend.modules.corebanking.application.port.in;

import com.thinhpay.backend.modules.corebanking.application.dto.request.DepositRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.response.AccountResponse;

public interface DepositUseCase {
    AccountResponse deposit(DepositRequest depositRequest);
}
