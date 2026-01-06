package com.thinhpay.backend.modules.corebanking.application.port.in;

import com.thinhpay.backend.modules.corebanking.application.dto.req.DepositRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.res.AccountResponse;

public interface DepositUseCase {
    /**
 * Performs a deposit operation using the provided deposit request.
 *
 * @param depositRequest the deposit details, including amount and target account information
 * @return the account state after the deposit has been applied
 */
AccountResponse deposit(DepositRequest depositRequest);
}