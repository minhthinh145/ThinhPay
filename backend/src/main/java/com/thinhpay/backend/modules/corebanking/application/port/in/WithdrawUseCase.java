package com.thinhpay.backend.modules.corebanking.application.port.in;

import com.thinhpay.backend.modules.corebanking.application.dto.req.WithdrawRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.res.AccountResponse;

public interface WithdrawUseCase {
    /**
 * Performs a withdrawal from an account using the details provided in the request.
 *
 * @param request the withdrawal request containing the account identifier, amount, and any required metadata
 * @return an AccountResponse reflecting the account's state after the withdrawal, including updated balances and any transaction identifiers
 */
AccountResponse withdraw(WithdrawRequest request);
}