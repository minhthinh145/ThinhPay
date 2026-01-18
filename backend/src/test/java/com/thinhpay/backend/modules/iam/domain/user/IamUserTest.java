package com.thinhpay.backend.modules.iam.domain.user;

import com.thinhpay.backend.shared.domain.Email;
import com.thinhpay.backend.shared.domain.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho IamUser entity.
 * Test domain logic và business rules.
 */
@DisplayName("IamUser Domain Tests")
class IamUserTest {

    @Test
    @DisplayName("Should create new user with pending verification status")
    void testCreateNewUser() {
        // Given
        String email = "test@example.com";
        String phone = "+84901234567";
        String passwordHash = "hashed_password";
        String fullName = "Test User";

        // When
        IamUser user = IamUser.createNew(email, phone, passwordHash, fullName);

        // Then
        assertNotNull(user);
        assertEquals(email, user.getEmailValue());
        assertEquals(phone, user.getPhoneValue());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(fullName, user.getFullName());
        assertEquals(UserAccountStatus.PENDING_VERIFICATION, user.getStatus());
        assertEquals("USER", user.getRoleId());
        assertFalse(user.getEmailVerified());
        assertFalse(user.getPhoneVerified());
        assertFalse(user.getKycVerified());
        assertEquals(KycLevel.BASIC, user.getKycLevel());
    }

    @Test
    @DisplayName("Should verify email successfully")
    void testVerifyEmail() {
        // Given
        IamUser user = createTestUser();
        assertFalse(user.getEmailVerified());

        // When
        user.verifyEmail();

        // Then
        assertTrue(user.getEmailVerified());
    }

    @Test
    @DisplayName("Should throw exception when verify email twice")
    void testVerifyEmailTwice() {
        // Given
        IamUser user = createTestUser();
        user.verifyEmail();

        // When & Then
        assertThrows(IllegalStateException.class, user::verifyEmail);
    }

    @Test
    @DisplayName("Should verify phone successfully")
    void testVerifyPhone() {
        // Given
        IamUser user = createTestUser();
        assertFalse(user.getPhoneVerified());

        // When
        user.verifyPhone();

        // Then
        assertTrue(user.getPhoneVerified());
    }

    @Test
    @DisplayName("Should activate account after email and phone verified")
    void testActivateAccountAfterVerification() {
        // Given
        IamUser user = createTestUser();
        assertEquals(UserAccountStatus.PENDING_VERIFICATION, user.getStatus());

        // When
        user.verifyEmail();
        user.verifyPhone();

        // Then
        assertEquals(UserAccountStatus.ACTIVE, user.getStatus());
    }

    @Test
    @DisplayName("Should allow login only when account is active")
    void testCanLogin() {
        // Given
        IamUser user = createTestUser();

        // When & Then
        assertFalse(user.canLogin()); // Not verified yet

        user.verifyEmail();
        user.verifyPhone();
        assertTrue(user.canLogin()); // Now active
    }

    @Test
    @DisplayName("Should upgrade KYC level successfully")
    void testUpgradeKycLevel() {
        // Given
        IamUser user = createTestUser();
        assertEquals(KycLevel.BASIC, user.getKycLevel());

        // When
        user.upgradeKycLevel(KycLevel.ADVANCED);

        // Then
        assertEquals(KycLevel.ADVANCED, user.getKycLevel());
        assertTrue(user.getKycVerified());
    }

    @Test
    @DisplayName("Should not downgrade KYC level")
    void testCannotDowngradeKycLevel() {
        // Given
        IamUser user = createTestUser();
        user.upgradeKycLevel(KycLevel.PREMIUM);

        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> user.upgradeKycLevel(KycLevel.BASIC));
    }

    @Test
    @DisplayName("Should lock account successfully")
    void testLockAccount() {
        // Given
        IamUser user = createTestUser();
        user.verifyEmail();
        user.verifyPhone();
        assertEquals(UserAccountStatus.ACTIVE, user.getStatus());

        // When
        user.lock("Suspicious activity");

        // Then
        assertEquals(UserAccountStatus.LOCKED, user.getStatus());
        assertFalse(user.canLogin());
    }

    @Test
    @DisplayName("Should suspend account successfully")
    void testSuspendAccount() {
        // Given
        IamUser user = createTestUser();
        user.verifyEmail();
        user.verifyPhone();

        // When
        user.suspend("Policy violation");

        // Then
        assertEquals(UserAccountStatus.SUSPENDED, user.getStatus());
        assertFalse(user.canLogin());
    }

    @Test
    @DisplayName("Should reactivate account successfully")
    void testReactivateAccount() {
        // Given
        IamUser user = createTestUser();
        user.verifyEmail();
        user.verifyPhone();
        user.lock("Test");

        // When
        user.reactivate();

        // Then
        assertEquals(UserAccountStatus.ACTIVE, user.getStatus());
        assertTrue(user.canLogin());
    }

    @Test
    @DisplayName("Should not reactivate if not verified")
    void testCannotReactivateWithoutVerification() {
        // Given
        IamUser user = createTestUser();

        // When & Then
        assertThrows(IllegalStateException.class, user::reactivate);
    }

    @Test
    @DisplayName("Should update profile successfully")
    void testUpdateProfile() {
        // Given
        IamUser user = createTestUser();
        String newName = "Updated Name";
        String avatarUrl = "https://example.com/avatar.jpg";

        // When
        user.updateProfile(newName, avatarUrl);

        // Then
        assertEquals(newName, user.getFullName());
        assertEquals(avatarUrl, user.getAvatarUrl());
    }

    @Test
    @DisplayName("Should change password successfully")
    void testChangePassword() {
        // Given
        IamUser user = createTestUser();
        String newPasswordHash = "new_hashed_password";

        // When
        user.changePassword(newPasswordHash);

        // Then
        assertEquals(newPasswordHash, user.getPasswordHash());
    }

    @Test
    @DisplayName("Should not change password with blank value")
    void testCannotChangePasswordWithBlank() {
        // Given
        IamUser user = createTestUser();

        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> user.changePassword(""));
    }

    @Test
    @DisplayName("Should check if user can perform transaction")
    void testCanPerformTransaction() {
        // Given
        IamUser user = createTestUser();

        // When & Then
        assertFalse(user.canPerformTransaction()); // Not verified

        user.verifyEmail();
        assertFalse(user.canPerformTransaction()); // Only email verified

        user.verifyPhone();
        assertTrue(user.canPerformTransaction()); // Both verified
    }

    @Test
    @DisplayName("Should check if user is fully verified")
    void testIsFullyVerified() {
        // Given
        IamUser user = createTestUser();

        // When & Then
        assertFalse(user.isFullyVerified());

        user.verifyEmail();
        user.verifyPhone();
        assertFalse(user.isFullyVerified()); // KYC not verified yet

        user.upgradeKycLevel(KycLevel.ADVANCED);
        assertTrue(user.isFullyVerified()); // All verified
    }

    // Helper method
    private IamUser createTestUser() {
        return IamUser.createNew(
            "test@example.com",
            "+84901234567",
            "hashed_password",
            "Test User"
        );
    }
}
