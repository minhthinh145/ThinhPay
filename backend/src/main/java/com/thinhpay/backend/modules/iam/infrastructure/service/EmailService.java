package com.thinhpay.backend.modules.iam.infrastructure.service;

import com.thinhpay.backend.shared.infrastructure.util.EmailTemplateLoader;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Email Service - Sends OTP and notification emails.
 *
 * Uses EmailTemplateLoader for clean separation of email templates.
 * All HTML templates are stored in resources/email-templates/
 *
 * @author ThinhPay Team
 * @since 2026-01-18 (Refactored)
 */
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    JavaMailSender mailSender;
    EmailTemplateLoader templateLoader;
    String fromEmail;
    String fromName;
    String appName;

    public EmailService(
            JavaMailSender mailSender,
            EmailTemplateLoader templateLoader,
            @Value("${thinhpay.email.from}") String fromEmail,
            @Value("${thinhpay.email.from-name}") String fromName,
            @Value("${thinhpay.app.name}") String appName
    ) {
        this.mailSender = mailSender;
        this.templateLoader = templateLoader;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.appName = appName;
    }

    /**
     * Send verification OTP email.
     */
    @Async
    public void sendVerificationOtp(String toEmail, String otpCode, String fullName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("appName", appName);
        variables.put("fullName", fullName);
        variables.put("otpCode", otpCode);
        variables.put("purpose", "xác thực tài khoản");

        String content = templateLoader.loadAndProcess("otp-verification.html", variables);
        String subject = "Xác thực tài khoản " + appName;
        sendHtmlEmail(toEmail, subject, content);
    }

    /**
     * Send login OTP email (2FA).
     */
    @Async
    public void sendLoginOtp(String toEmail, String otpCode, String fullName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("appName", appName);
        variables.put("fullName", fullName);
        variables.put("otpCode", otpCode);
        variables.put("loginTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        variables.put("ipAddress", "N/A"); // TODO: Get actual IP from request
        variables.put("deviceInfo", "N/A"); // TODO: Get actual device info

        String content = templateLoader.loadAndProcess("otp-login.html", variables);
        String subject = "Mã OTP đăng nhập " + appName;
        sendHtmlEmail(toEmail, subject, content);
    }

    /**
     * Send transaction OTP email.
     */
    @Async
    public void sendTransactionOtp(String toEmail, String otpCode, String fullName, String transactionInfo) {
        Map<String, String> variables = new HashMap<>();
        variables.put("appName", appName);
        variables.put("fullName", fullName);
        variables.put("otpCode", otpCode);
        variables.put("transactionInfo", transactionInfo);

        String content = templateLoader.loadAndProcess("otp-transaction.html", variables);
        String subject = "Mã OTP xác nhận giao dịch - " + appName;
        sendHtmlEmail(toEmail, subject, content);
    }

    /**
     * Send password reset OTP email.
     */
    @Async
    public void sendPasswordResetOtp(String toEmail, String otpCode, String fullName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("appName", appName);
        variables.put("fullName", fullName);
        variables.put("email", toEmail);
        variables.put("otpCode", otpCode);

        String content = templateLoader.loadAndProcess("otp-password-reset.html", variables);
        String subject = "Mã OTP đặt lại mật khẩu - " + appName;
        sendHtmlEmail(toEmail, subject, content);
    }

    /**
     * Send HTML email (private helper method).
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email to: {} - Error: {}", to, e.getMessage());
            // Don't throw exception - email failure should not block user operations
        }
    }
}
