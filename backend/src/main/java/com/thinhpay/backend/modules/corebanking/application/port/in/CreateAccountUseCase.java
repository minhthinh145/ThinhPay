package com.thinhpay.backend.modules.corebanking.application.port.in;

import com.thinhpay.backend.modules.corebanking.application.dto.request.CreateAccountRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.response.AccountResponse;

public interface CreateAccountUseCase {
    AccountResponse createAccount(CreateAccountRequest request);
}
