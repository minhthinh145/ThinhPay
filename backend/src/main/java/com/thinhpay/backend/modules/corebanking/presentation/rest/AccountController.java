package com.thinhpay.backend.modules.corebanking.presentation.rest;

import com.thinhpay.backend.modules.corebanking.application.dto.req.DepositRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.res.AccountResponse;
import com.thinhpay.backend.modules.corebanking.application.port.in.DepositUseCase;
import com.thinhpay.backend.shared.presentation.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountController {
    DepositUseCase depositUseCase;

    /**
     * Perform a deposit for an account using the provided request data.
     *
     * @param depositRequest the deposit details (for example, account identifier and amount)
     * @return an ApiResponse containing the updated AccountResponse and a success message
     */
    @PostMapping("/deposit")
    public ApiResponse<AccountResponse> deposit(@RequestBody @Valid DepositRequest depositRequest) {
        AccountResponse accountResponse = depositUseCase.deposit(depositRequest);
        return ApiResponse.success(accountResponse, "Deposit successful");
    }
}