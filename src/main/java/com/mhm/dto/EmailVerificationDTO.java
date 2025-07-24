package com.mhm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationDTO {
    private String email;
    private String token;
    private String verificationCode;
    private String verificationLink;
    
    // User information
    private String userId;
    private String firstName;
    private String lastName;
    private String username;
    
    // Verification details
    private String verificationType; // registration, email_change, password_reset, etc.
    private LocalDateTime tokenGeneratedAt;
    private LocalDateTime tokenExpiresAt;
    private int tokenValidityMinutes;
    
    // Status tracking
    private String status; // pending, sent, verified, expired, failed
    private LocalDateTime sentAt;
    private LocalDateTime verifiedAt;
    private int attemptCount;
    private int maxAttempts;
    
    // Security settings
    private boolean requiresSecureLink;
    private String ipAddress;
    private String userAgent;
    private String redirectUrl;
    
    // Template settings
    private String emailSubject;
    private String emailTemplateId;
    private String customMessage;
    
    // Resend settings
    private boolean canResend;
    private LocalDateTime lastResendAt;
    private int resendCount;
    private int maxResends;
    private int resendCooldownMinutes;
    
    // Convenience constructor for registration verification
    public EmailVerificationDTO(String email, String token, String firstName, String lastName) {
        this.email = email;
        this.token = token;
        this.firstName = firstName;
        this.lastName = lastName;
        this.verificationType = "registration";
        this.tokenGeneratedAt = LocalDateTime.now();
        this.tokenValidityMinutes = 60; // 1 hour default
        this.tokenExpiresAt = this.tokenGeneratedAt.plusMinutes(this.tokenValidityMinutes);
        this.status = "pending";
        this.maxAttempts = 3;
        this.maxResends = 3;
        this.resendCooldownMinutes = 5;
        this.canResend = true;
    }
    
    // Constructor for password reset verification
    public EmailVerificationDTO(String email, String token, String verificationType, int validityMinutes) {
        this.email = email;
        this.token = token;
        this.verificationType = verificationType;
        this.tokenGeneratedAt = LocalDateTime.now();
        this.tokenValidityMinutes = validityMinutes;
        this.tokenExpiresAt = this.tokenGeneratedAt.plusMinutes(this.tokenValidityMinutes);
        this.status = "pending";
        this.maxAttempts = 3;
        this.maxResends = 3;
        this.resendCooldownMinutes = 5;
        this.canResend = true;
    }
    
    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.tokenExpiresAt);
    }
    
    public boolean canAttemptVerification() {
        return this.attemptCount < this.maxAttempts && !isExpired();
    }
    
    public boolean canResendEmail() {
        if (!this.canResend || this.resendCount >= this.maxResends) {
            return false;
        }
        
        if (this.lastResendAt != null) {
            return LocalDateTime.now().isAfter(this.lastResendAt.plusMinutes(this.resendCooldownMinutes));
        }
        
        return true;
    }
    
    public void incrementAttemptCount() {
        this.attemptCount++;
    }
    
    public void incrementResendCount() {
        this.resendCount++;
        this.lastResendAt = LocalDateTime.now();
    }
    
    public void markAsVerified() {
        this.status = "verified";
        this.verifiedAt = LocalDateTime.now();
    }
    
    public void markAsSent() {
        this.status = "sent";
        this.sentAt = LocalDateTime.now();
    }
    
    // Static factory methods
    public static EmailVerificationDTO forRegistration(String email, String token, String firstName, String lastName) {
        return new EmailVerificationDTO(email, token, firstName, lastName);
    }
    
    public static EmailVerificationDTO forPasswordReset(String email, String token) {
        return new EmailVerificationDTO(email, token, "password_reset", 30); // 30 minutes for password reset
    }
    
    public static EmailVerificationDTO forEmailChange(String email, String token) {
        return new EmailVerificationDTO(email, token, "email_change", 60); // 1 hour for email change
    }
} 