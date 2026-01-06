package com.thinhpay.backend.modules.corebanking.application.port.in;

import com.thinhpay.backend.modules.corebanking.application.dto.req.DepositRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.res.AccountResponse;

public interface DepositUseCase {
    AccountResponse deposit(DepositRequest depositRequest);
}
