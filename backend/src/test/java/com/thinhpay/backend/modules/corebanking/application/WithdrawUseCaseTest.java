package com.thinhpay.backend.modules.corebanking.application;

import com.thinhpay.backend.BaseIntegrationTest;
import com.thinhpay.backend.modules.corebanking.application.dto.request.WithdrawRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.response.AccountResponse;
import com.thinhpay.backend.modules.corebanking.application.port.in.WithdrawUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WithdrawUseCaseTest extends BaseIntegrationTest {

    @Autowired
    private WithdrawUseCase withdrawUseCase;

    @Test
    @DisplayName("Rút tiền thành công - Số dư giảm chính xác")
    void withdraw_ShouldSuccess_WhenBalanceIsEnough() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        createTestAccount(userId, "1000.00");

        WithdrawRequest request = new WithdrawRequest(userId, new BigDecimal("400.00"), "REQ-W-001");

        // WHEN
        AccountResponse response = withdrawUseCase.withdraw(request);

        // THEN
        assertThat(response.getBalance()).isEqualByComparingTo("600.00");
        var accountInDb = accountRepository.findByUserId(userId).get();
        assertThat(accountInDb.getBalance()).isEqualByComparingTo("600.00");
    }

    @Test
    @DisplayName("Rút tiền thất bại - Số dư không đủ")
    void withdraw_ShouldThrowException_WhenInsufficientBalance() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        createTestAccount(userId, "100.00");

        WithdrawRequest request = new WithdrawRequest(userId, new BigDecimal("150.00"), "REQ-W-002");

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> {
            withdrawUseCase.withdraw(request);
        });

        var accountInDb = accountRepository.findByUserId(userId).get();
        assertThat(accountInDb.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Idempotency: Chống rút tiền trùng lặp với cùng requestId")
    void withdraw_ShouldHandleIdempotency() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        createTestAccount(userId, "500.00");
        String requestId = "UNIQUE-WITHDRAW-ID";
        WithdrawRequest request = new WithdrawRequest(userId, new BigDecimal("100.00"), requestId);

        // Lần 1
        withdrawUseCase.withdraw(request);

        // Lần 2 (Gửi lại cùng requestId)
        assertThrows(IllegalStateException.class, () -> {
            withdrawUseCase.withdraw(request);
        });

        var accountInDb = accountRepository.findByUserId(userId).get();
        assertThat(accountInDb.getBalance()).isEqualByComparingTo("400.00");
    }

    @Test
    @DisplayName("Concurrency: 10 luồng cùng rút tiền - Kiểm tra Pessimistic Lock")
    void withdraw_Concurrency_ShouldUpdateBalanceCorrectly() throws InterruptedException {
        // 1. GIVEN: Tạo dữ liệu mẫu
        UUID userId = UUID.randomUUID();
        createTestAccount(userId, "1000.00");

        // QUAN TRỌNG: Ép commit dữ liệu GIVEN xuống Database thực tế
        // Vì các thread con chạy ở Connection khác sẽ không thấy data nếu chưa commit
        if (TestTransaction.isActive()) {
            TestTransaction.flagForCommit();
            TestTransaction.end();
            TestTransaction.start();
        }

        int numberOfThreads = 10;
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        // 2. WHEN: Kích hoạt 10 luồng rút tiền đồng thời
        for (int i = 0; i < numberOfThreads; i++) {
            String requestId = "REQ-CONCURRENCY-" + i;
            executorService.execute(() -> {
                try {
                    startLatch.await(); // Chờ lệnh xuất phát
                    withdrawUseCase.withdraw(new WithdrawRequest(userId, withdrawAmount, requestId));
                } catch (Exception e) {
                    System.err.println("Thread Error: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // BẮT ĐẦU!
        boolean finished = doneLatch.await(15, TimeUnit.SECONDS);
        executorService.shutdown();

        // 3. THEN: Kết quả phải là 0.00 (1000 - 100*10)
        var accountInDb = accountRepository.findByUserId(userId).orElseThrow();
        System.out.println("Final Balance after concurrency: " + accountInDb.getBalance());

        assertThat(accountInDb.getBalance()).isEqualByComparingTo("0.00");
    }
}