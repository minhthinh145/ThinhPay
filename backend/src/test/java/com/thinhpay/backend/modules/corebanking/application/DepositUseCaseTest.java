package com.thinhpay.backend.modules.corebanking.application;

import com.thinhpay.backend.BaseIntegrationTest;
import com.thinhpay.backend.modules.corebanking.application.dto.req.DepositRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.res.AccountResponse;
import com.thinhpay.backend.modules.corebanking.application.port.in.DepositUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DepositUseCaseTest extends BaseIntegrationTest { // Kế thừa từ Base

    @Autowired
    private DepositUseCase depositUseCase;

    @Test
    @DisplayName("Nạp tiền thành công vào tài khoản đã tồn tại")
    void should_DepositSuccessfully() {
        // 1. GIVEN: Sử dụng helper từ Base để tạo data
        UUID userId = UUID.randomUUID();
        createTestAccount(userId, "100.00"); // Đã có 100k

        DepositRequest request = new DepositRequest(userId, new BigDecimal("50.00"), "REQ-" + UUID.randomUUID());

        // 2. WHEN: Thực hiện nạp thêm 50k
        AccountResponse response = depositUseCase.deposit(request);

        // 3. THEN: Assert kết quả
        assertThat(response.getBalance()).isEqualByComparingTo("150.00");

        // Kiểm tra chắc chắn trong DB cũng đã cập nhật
        var updatedAccount = accountRepository.findByUserId(userId).get();
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("Idempotency: Gửi trùng request ID second should throw exception")
    void should_ThrowException_For_DuplicateRequestId(){
        //Given
        UUID userId = UUID.randomUUID();
        createTestAccount(userId, "100.00");

        String commonRequestId = "unique-req-id-001";
        DepositRequest request = new DepositRequest(userId, new BigDecimal("50.00"), commonRequestId);

        //deposit first time
        depositUseCase.deposit(request);

        // when - then : deposit second time with same requestId should throw exception
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            depositUseCase.deposit(request);
        });

        //Check balance remain unchanged
        var account = accountRepository.findByUserId(userId).orElseThrow();
        assertThat(account.getBalance()).isEqualByComparingTo("150.00");
    }
}