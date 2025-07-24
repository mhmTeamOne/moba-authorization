package com.mhm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {
    private String filename;
    private String contentType;
    private byte[] content;
    private String contentId; // For inline attachments
    private String disposition; // attachment or inline
    
    // Convenience constructor for simple attachments
    public AttachmentDTO(String filename, String contentType, byte[] content) {
        this.filename = filename;
        this.contentType = contentType;
        this.content = content;
        this.disposition = "attachment";
    }
} 