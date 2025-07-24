package com.mhm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkEmailDTO {
    private String batchId;
    private String batchName;
    private List<EmailDTO> emails;
    private List<String> recipients;
    
    // Common email properties for bulk sending
    private String subject;
    private String textContent;
    private String htmlContent;
    private String templateId;
    private Map<String, Object> commonTemplateData;
    
    // Sender information
    private String from;
    private String fromName;
    
    // Processing settings
    private int batchSize;
    private int delayBetweenBatches; // in seconds
    private boolean parallel;
    private String priority; // high, medium, low
    
    // Scheduling
    private LocalDateTime scheduledAt;
    private boolean immediate;
    
    // Status tracking
    private String status; // pending, processing, completed, failed, paused
    private int totalEmails;
    private int sentCount;
    private int failedCount;
    private int pendingCount;
    
    // Metadata
    private String category;
    private String campaign;
    private Map<String, Object> metadata;
    
    // Tracking settings
    private boolean trackOpens;
    private boolean trackClicks;
    private boolean trackUnsubscribes;
    
    // Error handling
    private int maxRetries;
    private String onFailureAction; // stop, continue, pause
    
    // Convenience constructor for simple bulk email
    public BulkEmailDTO(List<String> recipients, String subject, String textContent) {
        this.recipients = recipients;
        this.subject = subject;
        this.textContent = textContent;
        this.totalEmails = recipients != null ? recipients.size() : 0;
        this.status = "pending";
        this.immediate = true;
    }
    
    // Constructor for template-based bulk email
    public BulkEmailDTO(List<String> recipients, String templateId, Map<String, Object> commonTemplateData) {
        this.recipients = recipients;
        this.templateId = templateId;
        this.commonTemplateData = commonTemplateData;
        this.totalEmails = recipients != null ? recipients.size() : 0;
        this.status = "pending";
        this.immediate = true;
    }
    
    // Constructor for scheduled bulk email
    public BulkEmailDTO(List<String> recipients, String subject, String textContent, LocalDateTime scheduledAt) {
        this.recipients = recipients;
        this.subject = subject;
        this.textContent = textContent;
        this.scheduledAt = scheduledAt;
        this.totalEmails = recipients != null ? recipients.size() : 0;
        this.status = "pending";
        this.immediate = false;
    }
} 