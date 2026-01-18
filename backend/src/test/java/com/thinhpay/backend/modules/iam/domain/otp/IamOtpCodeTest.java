package com.thinhpay.backend.modules.iam.domain.otp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho IamOtpCode entity.
 */
@DisplayName("IamOtpCode Domain Tests")
class IamOtpCodeTest {

    @Test
    @DisplayName("Should generate OTP with 6 digits")
    void testGenerateOtp() {
        // Given
        UUID userId = UUID.randomUUID();

        // When
        IamOtpCode otp = IamOtpCode.generate(userId, OtpType.EMAIL, OtpPurpose.VERIFY_EMAIL);

        // Then
        assertNotNull(otp);
        assertEquals(userId, otp.getUserId());
        assertEquals(OtpType.EMAIL, otp.getType());
        assertEquals(OtpPurpose.VERIFY_EMAIL, otp.getPurpose());
        assertEquals(6, otp.getCode().length());
        assertTrue(otp.getCode().matches("\\d{6}")); // 6 digits
        assertFalse(otp.getVerified());
        assertEquals(0, otp.getAttempts());
        assertNotNull(otp.getExpiresAt());
    }

    @Test
    @DisplayName("Should verify OTP successfully with correct code")
    void testVerifyOtpSuccess() {
        // Given
        IamOtpCode otp = IamOtpCode.generate(
            UUID.randomUUID(),
            OtpType.EMAIL,
            OtpPurpose.VERIFY_EMAIL
        );
        String correctCode = otp.getCode();

        // When
        boolean result = otp.verify(correctCode);

        // Then
        assertTrue(result);
        assertTrue(otp.getVerified());
        assertEquals(1, otp.getAttempts()); // Attempts increased then check
    }

    @Test
    @DisplayName("Should fail verification with wrong code")
    void testVerifyOtpFailed() {
        // Given
        IamOtpCode otp = IamOtpCode.generate(
            UUID.randomUUID(),
            OtpType.EMAIL,
            OtpPurpose.VERIFY_EMAIL
        );
        String wrongCode = "000000";

        // When
        boolean result = otp.verify(wrongCode);

        // Then
        assertFalse(result);
        assertFalse(otp.getVerified());
        assertEquals(1, otp.getAttempts()); // Attempts increased
    }

    @Test
    @DisplayName("Should increment attempts on failed verification")
    void testIncrementAttempts() {
        // Given
        IamOtpCode otp = IamOtpCode.generate(
            UUID.randomUUID(),
            OtpType.EMAIL,
            OtpPurpose.LOGIN
        );

        // When
        otp.verify("111111");
        otp.verify("222222");
        otp.verify("333333");

        // Then
        assertEquals(3, otp.getAttempts());
        assertFalse(otp.getVerified());
    }

    @Test
    @DisplayName("Should detect expired OTP")
    void testIsExpired() {
        // Given
        IamOtpCode otp = IamOtpCode.builder()
            .userId(UUID.randomUUID())
            .code("123456")
            .type(OtpType.EMAIL)
            .purpose(OtpPurpose.VERIFY_EMAIL)
            .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES)) // Expired 1 minute ago
            .verified(false)
            .attempts(0)
            .build();

        // When & Then
        assertTrue(otp.isExpired());
    }

    @Test
    @DisplayName("Should detect non-expired OTP")
    void testIsNotExpired() {
        // Given
        IamOtpCode otp = IamOtpCode.generate(
            UUID.randomUUID(),
            OtpType.EMAIL,
            OtpPurpose.VERIFY_EMAIL
        );

        // When & Then
        assertFalse(otp.isExpired());
    }

    @Test
    @DisplayName("Should not verify expired OTP even with correct code")
    void testCannotVerifyExpiredOtp() {
        // Given
        IamOtpCode otp = IamOtpCode.builder()
            .userId(UUID.randomUUID())
            .code("123456")
            .type(OtpType.EMAIL)
            .purpose(OtpPurpose.VERIFY_EMAIL)
            .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
            .verified(false)
            .attempts(0)
            .build();

        // When & Then - Should throw IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> otp.verify("123456"));
        assertEquals("OTP đã hết hạn", exception.getMessage());
        assertFalse(otp.getVerified());
    }

    @Test
    @DisplayName("Should not verify already verified OTP")
    void testCannotVerifyTwice() {
        // Given
        IamOtpCode otp = IamOtpCode.generate(
            UUID.randomUUID(),
            OtpType.EMAIL,
            OtpPurpose.VERIFY_EMAIL
        );
        String code = otp.getCode();
        otp.verify(code); // First verification

        // When & Then - Should throw IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> otp.verify(code));
        assertEquals("OTP đã được verify rồi", exception.getMessage());
        assertTrue(otp.getVerified()); // Still verified
    }

    @Test
    @DisplayName("Should generate different codes for different OTPs")
    void testGenerateDifferentCodes() {
        // Given
        UUID userId = UUID.randomUUID();

        // When
        IamOtpCode otp1 = IamOtpCode.generate(userId, OtpType.EMAIL, OtpPurpose.VERIFY_EMAIL);
        IamOtpCode otp2 = IamOtpCode.generate(userId, OtpType.EMAIL, OtpPurpose.VERIFY_EMAIL);

        // Then
        assertNotEquals(otp1.getCode(), otp2.getCode());
    }

    @Test
    @DisplayName("Should check max attempts exceeded")
    void testMaxAttemptsExceeded() {
        // Given
        IamOtpCode otp = IamOtpCode.generate(
            UUID.randomUUID(),
            OtpType.EMAIL,
            OtpPurpose.LOGIN
        );

        // When - 3 failed attempts
        otp.verify("000000");
        otp.verify("111111");
        otp.verify("222222");

        // Then - 4th attempt should throw exception
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> otp.verify("333333"));
        assertEquals("Đã vượt quá số lần thử (max 3)", exception.getMessage());
        assertEquals(3, otp.getAttempts());
    }
}
