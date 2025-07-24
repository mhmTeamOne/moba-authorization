package com.mhm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateDTO {
    private String to;
    private List<String> cc;
    private List<String> bcc;
    private String templateId;
    private String templateName;
    private Map<String, Object> templateData;
    private String from;
    private String fromName;
    
    // Template metadata
    private String category;
    private String priority; // high, medium, low
    private boolean trackOpens;
    private boolean trackClicks;
    
    // Convenience constructor for simple template emails
    public EmailTemplateDTO(String to, String templateId, Map<String, Object> templateData) {
        this.to = to;
        this.templateId = templateId;
        this.templateData = templateData;
    }
    
    // Constructor with template name
    public EmailTemplateDTO(String to, String templateId, String templateName, Map<String, Object> templateData) {
        this.to = to;
        this.templateId = templateId;
        this.templateName = templateName;
        this.templateData = templateData;
    }
} 