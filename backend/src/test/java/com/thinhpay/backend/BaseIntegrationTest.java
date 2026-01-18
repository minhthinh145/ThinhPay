package com.thinhpay.backend;

import com.thinhpay.backend.modules.corebanking.domain.account.Account;
import com.thinhpay.backend.modules.corebanking.domain.account.AccountStatus;
import com.thinhpay.backend.modules.corebanking.domain.currency.Currency;
import com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa.AccountRepository;
import com.thinhpay.backend.modules.corebanking.infrastructure.persistence.jpa.CurrencyRepository;
import com.thinhpay.backend.modules.iam.infrastructure.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired protected AccountRepository accountRepository;
    @Autowired protected CurrencyRepository currencyRepository;

    /**
     * Mock IAM dependencies để isolate CoreBanking tests.
     * CoreBanking tests không cần mail server thật.
     */
    @MockBean protected EmailService emailService;
    @MockBean protected JavaMailSender javaMailSender;

    protected Currency vnd;

    @BeforeEach
    void setUpBase() {
        // Tự động tạo Currency VND mẫu trước mỗi test case
        vnd = currencyRepository.findById("VND").orElseGet(() ->
                currencyRepository.save(Currency.of("VND", "₫", 0))
        );
    }

    protected Account createTestAccount(UUID userId, String balance) {
        // Sử dụng phương thức builder của SuperBuilder
        Account account = Account.builder()
                .userId(userId)
                .currency(vnd)
                .balance(new BigDecimal(balance))
                .heldBalance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
        return accountRepository.save(account);
    }
}