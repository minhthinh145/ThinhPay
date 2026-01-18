package com.thinhpay.backend.modules.corebanking.presentation.rest;

import com.thinhpay.backend.modules.corebanking.application.dto.request.DepositRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.request.TransferRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.request.WithdrawRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.response.AccountResponse;
import com.thinhpay.backend.modules.corebanking.application.dto.response.TransferResponse;
import com.thinhpay.backend.modules.corebanking.application.port.in.DepositUseCase;
import com.thinhpay.backend.modules.corebanking.application.port.in.TransferUseCase;
import com.thinhpay.backend.modules.corebanking.application.port.in.WithdrawUseCase;
import com.thinhpay.backend.modules.corebanking.application.service.AccountQueryService;
import com.thinhpay.backend.shared.presentation.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountController {
    DepositUseCase depositUseCase;
    WithdrawUseCase withdrawUseCase;
    TransferUseCase transferUseCase;
    AccountQueryService accountQueryService;

    @PostMapping("/deposit")
    public ApiResponse<AccountResponse> deposit(@RequestBody @Valid DepositRequest depositRequest) {
        AccountResponse accountResponse = depositUseCase.deposit(depositRequest);
        return ApiResponse.success(accountResponse, "Deposit successful");
    }


    @PostMapping("/withdraw")
    public ApiResponse<AccountResponse> withdraw(@RequestBody @Valid WithdrawRequest request) {
        return ApiResponse.success(withdrawUseCase.withdraw(request), "Withdraw successful");
    }

    @PostMapping("/transfer")
    public ApiResponse<TransferResponse> transfer(@RequestBody @Valid TransferRequest request) {
        return ApiResponse.success(transferUseCase.transfer(request), "Transfer completed");
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<List<AccountResponse>> getUserAccounts(@PathVariable UUID userId) {
        return ApiResponse.success(accountQueryService.getUserAccounts(userId));
    }

    @GetMapping("/users/{userId}/balance")
    public ApiResponse<AccountResponse> getBalance(
            @PathVariable UUID userId,
            @RequestParam String currency
    ) {
        return ApiResponse.success(accountQueryService.getAccountBalance(userId, currency));
    }
}
