package com.mhm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailConfigDTO {
    // Provider settings
    private String provider; // sendgrid, smtp, ses, etc.
    private String apiKey;
    private String apiUrl;
    
    // SMTP settings (if using SMTP)
    private String smtpHost;
    private int smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private boolean smtpTls;
    private boolean smtpAuth;
    
    // Default sender information
    private String defaultFromEmail;
    private String defaultFromName;
    private String defaultReplyTo;
    
    // Rate limiting
    private int maxEmailsPerMinute;
    private int maxEmailsPerHour;
    private int maxEmailsPerDay;
    
    // Retry settings
    private int maxRetries;
    private int retryDelaySeconds;
    private boolean exponentialBackoff;
    
    // Template settings
    private String defaultTemplateId;
    private String templateBaseUrl;
    private Map<String, String> templateMappings;
    
    // Tracking settings
    private boolean trackOpensDefault;
    private boolean trackClicksDefault;
    private boolean trackUnsubscribesDefault;
    
    // Security settings
    private boolean requireTls;
    private boolean validateDomains;
    private boolean allowSelfSigned;
    
    // Logging and monitoring
    private boolean enableLogging;
    private String logLevel; // debug, info, warn, error
    private boolean enableMetrics;
    private String webhookUrl;
    
    // Validation settings
    private boolean validateEmailAddresses;
    private boolean allowLocalDomains;
    private int maxSubjectLength;
    private int maxContentLength;
    
    // Backup provider settings
    private String backupProvider;
    private EmailConfigDTO backupConfig;
    
    // Feature flags
    private boolean enableBulkEmails;
    private boolean enableScheduledEmails;
    private boolean enableTemplateEmails;
    private boolean enableAttachments;
    
    // Environment settings
    private String environment; // dev, test, prod
    private boolean testMode;
    private String testEmailOverride;
    
    // Custom settings
    private Map<String, Object> customSettings;
    
    // Convenience constructor for basic SMTP configuration
    public EmailConfigDTO(String smtpHost, int smtpPort, String smtpUsername, String smtpPassword) {
        this.provider = "smtp";
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.smtpTls = true;
        this.smtpAuth = true;
    }
    
    // Constructor for SendGrid configuration
    public EmailConfigDTO(String apiKey, String defaultFromEmail, String defaultFromName) {
        this.provider = "sendgrid";
        this.apiKey = apiKey;
        this.defaultFromEmail = defaultFromEmail;
        this.defaultFromName = defaultFromName;
    }
    
    // Static factory method for SendGrid
    public static EmailConfigDTO sendGrid(String apiKey, String fromEmail, String fromName) {
        return new EmailConfigDTO(apiKey, fromEmail, fromName);
    }
    
    // Static factory method for SMTP
    public static EmailConfigDTO smtp(String host, int port, String username, String password) {
        return new EmailConfigDTO(host, port, username, password);
    }
} 