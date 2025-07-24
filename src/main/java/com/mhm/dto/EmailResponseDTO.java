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
public class EmailResponseDTO {
    private String messageId;
    private String status; // sent, failed, pending, delivered, bounced
    private String recipient;
    private String subject;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    
    // Response details
    private int statusCode;
    private String statusMessage;
    private String errorCode;
    private String errorMessage;
    
    // Provider specific data
    private String providerMessageId;
    private String providerResponse;
    
    // Tracking information
    private boolean opened;
    private boolean clicked;
    private LocalDateTime openedAt;
    private LocalDateTime clickedAt;
    private int openCount;
    private int clickCount;
    
    // Metadata
    private String emailType; // welcome, verification, notification, etc.
    private String templateId;
    private Map<String, Object> metadata;
    
    // Convenience constructor for success response
    public EmailResponseDTO(String messageId, String status, String recipient, String subject) {
        this.messageId = messageId;
        this.status = status;
        this.recipient = recipient;
        this.subject = subject;
        this.sentAt = LocalDateTime.now();
    }
    
    // Constructor for error response
    public EmailResponseDTO(String recipient, String errorCode, String errorMessage, boolean isError) {
        this.status = "failed";
        this.recipient = recipient;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.sentAt = LocalDateTime.now();
    }
    
    // Static factory method for success
    public static EmailResponseDTO success(String messageId, String recipient, String subject) {
        return new EmailResponseDTO(messageId, "sent", recipient, subject);
    }
    
    // Static factory method for failure
    public static EmailResponseDTO failure(String recipient, String errorCode, String errorMessage) {
        return new EmailResponseDTO(recipient, errorCode, errorMessage, true);
    }
} 