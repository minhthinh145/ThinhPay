package com.thinhpay.backend.modules.corebanking.application.port.in;

import com.thinhpay.backend.modules.corebanking.application.dto.req.CreateAccountRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.res.AccountResponse;

public interface CreateAccountUseCase {
    AccountResponse createAccount(CreateAccountRequest request);
}
