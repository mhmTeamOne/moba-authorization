package com.mhm.services;

import com.mhm.dto.AttachmentDTO;
import com.mhm.dto.EmailDTO;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@ApplicationScoped
public class TwilioEmailService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioEmailService.class);
    
    @ConfigProperty(name = "sendgrid.api.key")
    String sendGridApiKey;
    
    @ConfigProperty(name = "sendgrid.from.email")
    String defaultFromEmail;
    
    @ConfigProperty(name = "sendgrid.from.name")
    String defaultFromName;
    
    /**
     * Send email using Twilio SendGrid API
     */
    public String sendEmail(EmailDTO emailDTO) {
        try {
            LOGGER.info("SendGrid API Key format check - Length: {}, Starts with SG.: {}", 
                       sendGridApiKey.length(), sendGridApiKey.startsWith("SG."));
            
            if (sendGridApiKey.length() < 50 || !sendGridApiKey.startsWith("SG.")) {
                LOGGER.error("Invalid SendGrid API key format detected!");
            }
            
            SendGrid sg = new SendGrid(sendGridApiKey);
            
            // Create email components
            Email from = new Email(
                emailDTO.getFrom() != null ? emailDTO.getFrom() : defaultFromEmail,
                emailDTO.getFromName() != null ? emailDTO.getFromName() : defaultFromName
            );
            
            Email to = new Email(emailDTO.getTo());
            
            // Create content
            Content content = new Content("text/plain", emailDTO.getTextContent());
            
            // Create mail object
            Mail mail = new Mail(from, emailDTO.getSubject(), to, content);
            
            // Add HTML content if provided
            if (emailDTO.getHtmlContent() != null) {
                Content htmlContent = new Content("text/html", emailDTO.getHtmlContent());
                mail.addContent(htmlContent);
            }
            
            // Add CC recipients
            if (emailDTO.getCc() != null && !emailDTO.getCc().isEmpty()) {
                Personalization personalization = new Personalization();
                personalization.addTo(to);
                
                for (String ccEmail : emailDTO.getCc()) {
                    personalization.addCc(new Email(ccEmail));
                }
                
                mail.addPersonalization(personalization);
            }
            
            // Add BCC recipients
            if (emailDTO.getBcc() != null && !emailDTO.getBcc().isEmpty()) {
                Personalization personalization = mail.getPersonalization().get(0);
                if (personalization == null) {
                    personalization = new Personalization();
                    personalization.addTo(to);
                    mail.addPersonalization(personalization);
                }
                
                for (String bccEmail : emailDTO.getBcc()) {
                    personalization.addBcc(new Email(bccEmail));
                }
            }
            
            // Add attachments
            if (emailDTO.getAttachments() != null && !emailDTO.getAttachments().isEmpty()) {
                for (AttachmentDTO attachmentDTO : emailDTO.getAttachments()) {
                    Attachments attachment = new Attachments();
                    attachment.setFilename(attachmentDTO.getFilename());
                    attachment.setType(attachmentDTO.getContentType());
                    attachment.setDisposition(attachmentDTO.getDisposition());
                    attachment.setContent(java.util.Base64.getEncoder().encodeToString(attachmentDTO.getContent()));
                    
                    if (attachmentDTO.getContentId() != null) {
                        attachment.setContentId(attachmentDTO.getContentId());
                    }
                    
                    mail.addAttachments(attachment);
                }
            }
            
            // Send email
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            LOGGER.info("Email sent successfully. Status code: {}", response.getStatusCode());
            LOGGER.debug("Response body: {}", response.getBody());
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                return "Email sent successfully";
            } else {
                LOGGER.error("Failed to send email. Status code: {}, Body: {}", 
                           response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to send email: " + response.getBody());
            }
            
        } catch (IOException e) {
            LOGGER.error("Error sending email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send welcome email to new users
     */
    public String sendWelcomeEmail(String userEmail, String userName) {
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setTo(userEmail);
        emailDTO.setSubject("Welcome to MOBA Authorization!");
        emailDTO.setTextContent("Hello " + userName + ",\n\nWelcome to MOBA Authorization system. Your account has been created successfully.");
        emailDTO.setHtmlContent(
            "<html><body>" +
            "<h2>Welcome to MOBA Authorization!</h2>" +
            "<p>Hello <strong>" + userName + "</strong>,</p>" +
            "<p>Welcome to MOBA Authorization system. Your account has been created successfully.</p>" +
            "<p>Best regards,<br>MOBA Team</p>" +
            "</body></html>"
        );
        
        return sendEmail(emailDTO);
    }
    
    /**
     * Send password reset email
     */
    public String sendPasswordResetEmail(String userEmail, String resetToken) {
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setTo(userEmail);
        emailDTO.setSubject("Password Reset Request");
        emailDTO.setTextContent("Click the following link to reset your password: " + 
                               "http://localhost:8081/reset-password?token=" + resetToken);
        emailDTO.setHtmlContent(
            "<html><body>" +
            "<h2>Password Reset Request</h2>" +
            "<p>Click the following link to reset your password:</p>" +
            "<p><a href=\"http://localhost:8081/reset-password?token=" + resetToken + "\">Reset Password</a></p>" +
            "<p>If you didn't request this, please ignore this email.</p>" +
            "</body></html>"
        );
        
        return sendEmail(emailDTO);
    }
    
    /**
     * Send bulk emails
     */
    public String sendBulkEmails(List<EmailDTO> emails) {
        int successCount = 0;
        int failureCount = 0;
        
        for (EmailDTO email : emails) {
            try {
                sendEmail(email);
                successCount++;
            } catch (Exception e) {
                LOGGER.error("Failed to send bulk email to: {}", email.getTo(), e);
                failureCount++;
            }
        }
        
        return String.format("Bulk email completed. Success: %d, Failures: %d", successCount, failureCount);
    }
    
    /**
     * Process scheduled emails (placeholder for future implementation)
     */
    public String processScheduledEmails() {
        // This would typically fetch scheduled emails from database
        // and process them
        LOGGER.info("Processing scheduled emails...");
        return "Scheduled emails processed";
    }
    
    /**
     * Send email with template (example)
     */
    public String sendTemplatedEmail(String to, String templateId, Object templateData) {
        try {
            SendGrid sg = new SendGrid(sendGridApiKey);
            
            Email from = new Email(defaultFromEmail, defaultFromName);
            Email toEmail = new Email(to);
            
            Mail mail = new Mail();
            mail.setFrom(from);
            mail.setTemplateId(templateId);
            
            Personalization personalization = new Personalization();
            personalization.addTo(toEmail);
            
            // Add template data (this would need to be properly formatted)
            // personalization.addDynamicTemplateData("name", templateData);
            
            mail.addPersonalization(personalization);
            
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                return "Templated email sent successfully";
            } else {
                throw new RuntimeException("Failed to send templated email: " + response.getBody());
            }
            
        } catch (IOException e) {
            LOGGER.error("Error sending templated email", e);
            throw new RuntimeException("Failed to send templated email", e);
        }
    }
} 