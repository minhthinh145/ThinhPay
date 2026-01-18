package com.thinhpay.backend.modules.corebanking.application;

import com.thinhpay.backend.BaseIntegrationTest;
import com.thinhpay.backend.modules.corebanking.application.dto.request.TransferRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.response.TransferResponse;
import com.thinhpay.backend.modules.corebanking.application.port.in.TransferUseCase;
import com.thinhpay.backend.modules.corebanking.domain.account.Account;
import com.thinhpay.backend.modules.corebanking.domain.transaction.TransactionStatus;
import com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa.LedgerEntryRepository;
import com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransferServiceTest extends BaseIntegrationTest {

    @Autowired
    private TransferUseCase transferUseCase;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @AfterEach
    @org.springframework.transaction.annotation.Transactional
    void cleanUp() {
        // Clean ledger entries sau mỗi test để tránh data pollution
        ledgerEntryRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Chuyển tiền thành công giữa hai tài khoản")
    void should_TransferSuccessfully_BetweenTwoAccounts() {
        UUID senderUserId = UUID.randomUUID();
        UUID receiverUserId = UUID.randomUUID();
        Account senderAccount = createTestAccount(senderUserId, "1000.00");
        Account receiverAccount = createTestAccount(receiverUserId, "500.00");

        TransferRequest request = TransferRequest.builder()
                .requestId("TRANSFER-" + UUID.randomUUID())
                .senderUserId(senderUserId)
                .receiverUserId(receiverUserId)
                .amount(new BigDecimal("300.00"))
                .currency("VND")
                .description("Test transfer")
                .build();

        // WHEN: Thực hiện chuyển tiền
        TransferResponse response = transferUseCase.transfer(request);

        // THEN: Verify response (updated for multi-currency fields)
        assertThat(response.getSenderTransactionId()).isNotNull();
        assertThat(response.getReceiverTransactionId()).isNotNull();
        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
        assertThat(response.getDebitAmount()).isEqualByComparingTo("300.00");
        assertThat(response.getCreditAmount()).isEqualByComparingTo("300.00");
        assertThat(response.getFromCurrency()).isEqualTo("VND");
        assertThat(response.getToCurrency()).isEqualTo("VND");
        assertThat(response.getExchangeRate()).isEqualByComparingTo("1.000000");
        assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(response.getSenderAccountId()).isEqualTo(senderAccount.getId());
        assertThat(response.getReceiverAccountId()).isEqualTo(receiverAccount.getId());

        // Verify sender balance: 1000 - 300 = 700
        Account updatedSender = accountRepository.findById(senderAccount.getId()).orElseThrow();
        assertThat(updatedSender.getBalance()).isEqualByComparingTo("700.00");

        // Verify receiver balance: 500 + 300 = 800
        Account updatedReceiver = accountRepository.findById(receiverAccount.getId()).orElseThrow();
        assertThat(updatedReceiver.getBalance()).isEqualByComparingTo("800.00");

        // Verify 2 transactions được tạo
        assertThat(transactionRepository.findByRequestId(request.getRequestId())).isPresent();
        assertThat(transactionRepository.findByRequestId(request.getRequestId() + "-IN")).isPresent();

        // Verify 2 ledger entries được tạo
        assertThat(ledgerEntryRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ném lỗi khi số dư không đủ")
    void should_ThrowException_WhenInsufficientBalance() {
        // GIVEN: Sender có 100, muốn chuyển 200
        UUID senderUserId = UUID.randomUUID();
        UUID receiverUserId = UUID.randomUUID();

        createTestAccount(senderUserId, "100.00");
        createTestAccount(receiverUserId, "500.00");

        TransferRequest request = TransferRequest.builder()
                .requestId("TRANSFER-" + UUID.randomUUID())
                .senderUserId(senderUserId)
                .receiverUserId(receiverUserId)
                .amount(new BigDecimal("200.00"))
                .currency("VND")
                .description("Insufficient balance test")
                .build();

        // WHEN & THEN: Phải ném exception
        assertThrows(IllegalArgumentException.class, () -> {
            transferUseCase.transfer(request);
        });

        // Verify balance không thay đổi
        Account sender = accountRepository.findByUserId(senderUserId).orElseThrow();
        assertThat(sender.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Ném lỗi khi chuyển cho chính mình")
    void should_ThrowException_WhenTransferToSelf() {
        // GIVEN: Cùng 1 user
        UUID userId = UUID.randomUUID();
        createTestAccount(userId, "1000.00");

        TransferRequest request = TransferRequest.builder()
                .requestId("TRANSFER-" + UUID.randomUUID())
                .senderUserId(userId)
                .receiverUserId(userId)
                .amount(new BigDecimal("100.00"))
                .currency("VND")
                .description("Self transfer test")
                .build();

        // WHEN & THEN: Phải ném exception
        assertThrows(IllegalArgumentException.class, () -> {
            transferUseCase.transfer(request);
        });
    }

    @Test
    @DisplayName("Idempotency: Ném lỗi khi requestId trùng lặp")
    void should_ThrowException_WhenDuplicateRequestId() {
        // GIVEN
        UUID senderUserId = UUID.randomUUID();
        UUID receiverUserId = UUID.randomUUID();

        createTestAccount(senderUserId, "1000.00");
        createTestAccount(receiverUserId, "500.00");

        String requestId = "TRANSFER-DUPLICATE-TEST";
        TransferRequest request = TransferRequest.builder()
                .requestId(requestId)
                .senderUserId(senderUserId)
                .receiverUserId(receiverUserId)
                .amount(new BigDecimal("100.00"))
                .currency("VND")
                .description("Duplicate test")
                .build();

        // WHEN: Chuyển lần 1 thành công
        transferUseCase.transfer(request);

        // THEN: Chuyển lần 2 phải ném exception
        assertThrows(IllegalStateException.class, () -> {
            transferUseCase.transfer(request);
        });

        // Verify chỉ trừ 1 lần
        Account sender = accountRepository.findByUserId(senderUserId).orElseThrow();
        assertThat(sender.getBalance()).isEqualByComparingTo("900.00");
    }

    @Test
    @DisplayName("Ném lỗi khi tài khoản sender không tồn tại")
    void should_ThrowException_WhenSenderAccountNotFound() {
        // GIVEN: Chỉ tạo receiver
        UUID receiverUserId = UUID.randomUUID();
        createTestAccount(receiverUserId, "500.00");

        TransferRequest request = TransferRequest.builder()
                .requestId("TRANSFER-" + UUID.randomUUID())
                .senderUserId(UUID.randomUUID()) // User không tồn tại
                .receiverUserId(receiverUserId)
                .amount(new BigDecimal("100.00"))
                .currency("VND")
                .build();

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> {
            transferUseCase.transfer(request);
        });
    }

    @Test
    @DisplayName("Ném lỗi khi tài khoản receiver không tồn tại")
    void should_ThrowException_WhenReceiverAccountNotFound() {
        // GIVEN: Chỉ tạo sender
        UUID senderUserId = UUID.randomUUID();
        createTestAccount(senderUserId, "1000.00");

        TransferRequest request = TransferRequest.builder()
                .requestId("TRANSFER-" + UUID.randomUUID())
                .senderUserId(senderUserId)
                .receiverUserId(UUID.randomUUID()) // User không tồn tại
                .amount(new BigDecimal("100.00"))
                .currency("VND")
                .build();

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> {
            transferUseCase.transfer(request);
        });
    }
}
