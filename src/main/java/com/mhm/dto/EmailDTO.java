package com.mhm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDTO {
    private String to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String textContent;
    private String htmlContent;
    private String from;
    private String fromName;
    private List<AttachmentDTO> attachments;
    
    // Convenience constructor for simple emails
    public EmailDTO(String to, String subject, String textContent) {
        this.to = to;
        this.subject = subject;
        this.textContent = textContent;
    }
    
    // Convenience constructor for HTML emails
    public EmailDTO(String to, String subject, String textContent, String htmlContent) {
        this.to = to;
        this.subject = subject;
        this.textContent = textContent;
        this.htmlContent = htmlContent;
    }
} 