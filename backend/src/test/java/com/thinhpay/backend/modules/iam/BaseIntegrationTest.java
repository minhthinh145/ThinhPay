package com.thinhpay.backend.modules.iam;

import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import com.thinhpay.backend.modules.iam.domain.user.UserAccountStatus;
import com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa.IamUserRepository;
import com.thinhpay.backend.modules.iam.infrastructure.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class cho IAM integration tests.
 * Sử dụng PostgreSQL test database (Docker).
 * Follow pattern từ CoreBanking tests.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected IamUserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    /**
     * Mock EmailService để tránh cần mail server thật trong test.
     * Các test có thể verify behavior bằng Mockito.verify()
     */
    @MockBean
    protected EmailService emailService;

    /**
     * Helper method: Tạo test user với email đã verified.
     */
    protected IamUser createTestUser(String email, String password, String fullName) {
        IamUser user = IamUser.createNew(
            email,
            "+84901234567",
            passwordEncoder.encode(password),
            fullName
        );

        // Verify email và phone để user có thể login
        user.verifyEmail();
        user.verifyPhone();

        return userRepository.save(user);
    }

    /**
     * Helper method: Tạo pending user (chưa verify).
     */
    protected IamUser createPendingUser(String email, String password, String fullName) {
        IamUser user = IamUser.createNew(
            email,
            "+84987654321",
            passwordEncoder.encode(password),
            fullName
        );
        return userRepository.save(user);
    }

    /**
     * Helper method: Tạo locked user.
     */
    protected IamUser createLockedUser(String email, String password) {
        IamUser user = createTestUser(email, password, "Locked User");
        user.lock("Test lock");
        return userRepository.save(user);
    }

    @BeforeEach
    void cleanUpDatabase() {
        // Clean up test data before each test
        // Database sẽ tự rollback sau mỗi test do @Transactional
    }
}
