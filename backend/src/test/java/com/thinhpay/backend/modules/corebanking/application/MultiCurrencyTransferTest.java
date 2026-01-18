package com.thinhpay.backend.modules.corebanking.application;

import com.thinhpay.backend.BaseIntegrationTest;
import com.thinhpay.backend.modules.corebanking.application.dto.request.TransferRequest;
import com.thinhpay.backend.modules.corebanking.application.dto.response.TransferResponse;
import com.thinhpay.backend.modules.corebanking.application.port.in.TransferUseCase;
import com.thinhpay.backend.modules.corebanking.domain.account.Account;
import com.thinhpay.backend.modules.corebanking.domain.account.AccountStatus;
import com.thinhpay.backend.modules.corebanking.domain.currency.Currency;
import com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa.CurrencyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MultiCurrencyTransferTest extends BaseIntegrationTest {

    @Autowired
    private TransferUseCase transferUseCase;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Test
    @DisplayName("Should transfer successfully with same currency (backward compatible)")
    void should_TransferSuccessfully_WithSameCurrency() {
        // Given
        UUID sender = UUID.randomUUID();
        UUID receiver = UUID.randomUUID();

        Account senderAccount = createTestAccount(sender, "1000.00");
        Account receiverAccount = createTestAccount(receiver, "500.00");

        TransferRequest request = TransferRequest.builder()
                .requestId("TRANSFER-" + UUID.randomUUID())
                .senderUserId(sender)
                .receiverUserId(receiver)
                .amount(new BigDecimal("300.00"))
                .currency("VND")
                .description("Same currency transfer")
                .build();

        // When
        TransferResponse response = transferUseCase.transfer(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getDebitAmount()).isEqualByComparingTo("300.00");
        assertThat(response.getCreditAmount()).isEqualByComparingTo("300.00");
        assertThat(response.getExchangeRate()).isEqualByComparingTo("1.000000");

        Account updatedSender = accountRepository.findById(senderAccount.getId()).orElseThrow();
        assertThat(updatedSender.getBalance()).isEqualByComparingTo("700.00");

        Account updatedReceiver = accountRepository.findById(receiverAccount.getId()).orElseThrow();
        assertThat(updatedReceiver.getBalance()).isEqualByComparingTo("800.00");
    }

    // Helper method
    protected Account createAccountWithCurrency(UUID userId, String currencyCode, String balance) {
        Currency currency = currencyRepository.findById(currencyCode)
                .orElseGet(() -> currencyRepository.save(
                        Currency.of(currencyCode, getCurrencySymbol(currencyCode), 2)
                ));

        Account account = Account.builder()
                .userId(userId)
                .currency(currency)
                .balance(new BigDecimal(balance))
                .heldBalance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        return accountRepository.save(account);
    }

    private String getCurrencySymbol(String code) {
        return switch (code) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "VND" -> "₫";
            default -> code;
        };
    }
}