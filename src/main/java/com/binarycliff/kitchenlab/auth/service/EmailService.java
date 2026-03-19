package com.binarycliff.kitchenlab.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for sending emails (simplified implementation).
 * In production, integrate with a proper email service like SendGrid, AWS SES, etc.
 */
@Slf4j
@Service
public class EmailService {
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${app.email.from:noreply@kitchenlab.com}")
    private String fromEmail;
    
    @Value("${app.email.base-url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * Check if email service is enabled.
     */
    public boolean isEmailEnabled() {
        return emailEnabled;
    }
    
    /**
     * Send password reset email.
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        if (!emailEnabled) {
            log.info("Email service disabled. Password reset token for {}: {}", toEmail, resetToken);
            return;
        }
        
        String resetUrl = baseUrl + "/auth/reset-password?token=" + resetToken;
        
        String subject = "Password Reset - KitchenLab";
        String body = buildPasswordResetEmail(resetUrl);
        
        // In production, use a proper email service
        log.info("Sending password reset email to: {} with reset URL: {}", toEmail, resetUrl);
        // sendEmail(toEmail, subject, body);
    }
    
    /**
     * Send email verification email.
     */
    public void sendEmailVerification(String toEmail, UUID userId, String verificationToken) {
        if (!emailEnabled) {
            log.info("Email service disabled. Verification token for {}: {}", toEmail, verificationToken);
            return;
        }
        
        String verificationUrl = baseUrl + "/auth/verify-email?token=" + verificationToken;
        
        String subject = "Email Verification - KitchenLab";
        String body = buildEmailVerificationEmail(verificationUrl);
        
        // In production, use a proper email service
        log.info("Sending verification email to: {} with verification URL: {}", toEmail, verificationUrl);
        // sendEmail(toEmail, subject, body);
    }
    
    /**
     * Send welcome email.
     */
    public void sendWelcomeEmail(String toEmail, String username) {
        if (!emailEnabled) {
            log.info("Email service disabled. Welcome email for {}: {}", toEmail, username);
            return;
        }
        
        String subject = "Welcome to KitchenLab";
        String body = buildWelcomeEmail(username);
        
        // In production, use a proper email service
        log.info("Sending welcome email to: {} for user: {}", toEmail, username);
        // sendEmail(toEmail, subject, body);
    }
    
    private String buildPasswordResetEmail(String resetUrl) {
        return """
                <html>
                <body>
                    <h2>Password Reset Request</h2>
                    <p>Hello,</p>
                    <p>You requested a password reset for your KitchenLab account.</p>
                    <p>Click the link below to reset your password:</p>
                    <p><a href="%s">Reset Password</a></p>
                    <p>This link will expire in 1 hour.</p>
                    <p>If you didn't request this password reset, please ignore this email.</p>
                    <p>Best regards,<br/>KitchenLab Team</p>
                </body>
                </html>
                """.formatted(resetUrl);
    }
    
    private String buildEmailVerificationEmail(String verificationUrl) {
        return """
                <html>
                <body>
                    <h2>Email Verification</h2>
                    <p>Welcome to KitchenLab!</p>
                    <p>Please click the link below to verify your email address:</p>
                    <p><a href="%s">Verify Email</a></p>
                    <p>This link will expire in 24 hours.</p>
                    <p>Best regards,<br/>KitchenLab Team</p>
                </body>
                </html>
                """.formatted(verificationUrl);
    }
    
    private String buildWelcomeEmail(String username) {
        return """
                <html>
                <body>
                    <h2>Welcome to KitchenLab!</h2>
                    <p>Hello %s,</p>
                    <p>Your account has been successfully created.</p>
                    <p>You can now log in and start using KitchenLab.</p>
                    <p>Best regards,<br/>KitchenLab Team</p>
                </body>
                </html>
                """.formatted(username);
    }
}
