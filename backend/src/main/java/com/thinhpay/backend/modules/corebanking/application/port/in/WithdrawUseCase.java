package com.thinhpay.backend.modules.corebanking.application.port.in;

import com.thinhpay.backend.modules.corebanking.application.dto.req.WithdrawRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.res.AccountResponse;

public interface WithdrawUseCase {
    AccountResponse withdraw(WithdrawRequest request);
}
