package com.mhm.resources;

import com.mhm.dto.EmailDTO;
import com.mhm.services.TwilioEmailService;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Path("/email")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EmailResource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailResource.class);
    
    @Inject
    TwilioEmailService twilioEmailService;
    
    @Inject
    CamelContext camelContext;
    
    @Inject
    ProducerTemplate producerTemplate;
    
    /**
     * Send email using Camel route
     */
    @POST
    @Path("/send")
    public Response sendEmail(EmailDTO emailDTO) {
        try {
            String result = producerTemplate.requestBody("direct:send-email", emailDTO, String.class);
            return Response.ok(Map.of("message", result)).build();
        } catch (Exception e) {
            LOGGER.error("Error sending email via Camel route", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to send email: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Send email directly using TwilioEmailService
     */
    @POST
    @Path("/send-direct")
    public Response sendEmailDirect(EmailDTO emailDTO) {
        try {
            String result = twilioEmailService.sendEmail(emailDTO);
            return Response.ok(Map.of("message", result)).build();
        } catch (Exception e) {
            LOGGER.error("Error sending email directly", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to send email: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Send email using SMTP route
     */
    @POST
    @Path("/send-smtp")
    public Response sendEmailSmtp(EmailDTO emailDTO) {
        try {
            String result = producerTemplate.requestBody("direct:send-email-smtp", emailDTO, String.class);
            return Response.ok(Map.of("message", result)).build();
        } catch (Exception e) {
            LOGGER.error("Error sending email via SMTP route", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to send email via SMTP: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Send welcome email to new user
     */
    @POST
    @Path("/welcome")
    public Response sendWelcomeEmail(@QueryParam("email") String email, @QueryParam("name") String name) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Email is required"))
                    .build();
            }
            
            String result = twilioEmailService.sendWelcomeEmail(email, name != null ? name : "User");
            return Response.ok(Map.of("message", result)).build();
        } catch (Exception e) {
            LOGGER.error("Error sending welcome email", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to send welcome email: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Send password reset email
     */
    @POST
    @Path("/password-reset")
    public Response sendPasswordResetEmail(@QueryParam("email") String email, @QueryParam("token") String token) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Email is required"))
                    .build();
            }
            
            if (token == null || token.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Reset token is required"))
                    .build();
            }
            
            String result = twilioEmailService.sendPasswordResetEmail(email, token);
            return Response.ok(Map.of("message", result)).build();
        } catch (Exception e) {
            LOGGER.error("Error sending password reset email", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to send password reset email: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Send bulk emails
     */
    @POST
    @Path("/bulk")
    public Response sendBulkEmails(List<EmailDTO> emails) {
        try {
            if (emails == null || emails.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Email list cannot be empty"))
                    .build();
            }
            
            String result = producerTemplate.requestBody("direct:send-bulk-emails", emails, String.class);
            return Response.ok(Map.of("message", result)).build();
        } catch (Exception e) {
            LOGGER.error("Error sending bulk emails", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to send bulk emails: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Send templated email
     */
    @POST
    @Path("/template")
    public Response sendTemplatedEmail(
            @QueryParam("to") String to,
            @QueryParam("templateId") String templateId,
            Map<String, Object> templateData) {
        try {
            if (to == null || to.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Recipient email is required"))
                    .build();
            }
            
            if (templateId == null || templateId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Template ID is required"))
                    .build();
            }
            
            String result = twilioEmailService.sendTemplatedEmail(to, templateId, templateData);
            return Response.ok(Map.of("message", result)).build();
        } catch (Exception e) {
            LOGGER.error("Error sending templated email", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to send templated email: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Get email service status
     */
    @GET
    @Path("/status")
    public Response getEmailServiceStatus() {
        try {
            boolean camelContextRunning = camelContext.getStatus().isStarted();
            
            return Response.ok(Map.of(
                "service", "Email Service",
                "status", "running",
                "camel_context", camelContextRunning ? "running" : "stopped",
                "routes", camelContext.getRoutes().size(),
                "endpoints", List.of(
                    "POST /email/send - Send email via Camel route",
                    "POST /email/send-direct - Send email directly",
                    "POST /email/send-smtp - Send email via SMTP",
                    "POST /email/welcome - Send welcome email",
                    "POST /email/password-reset - Send password reset email",
                    "POST /email/bulk - Send bulk emails",
                    "POST /email/template - Send templated email"
                )
            )).build();
        } catch (Exception e) {
            LOGGER.error("Error getting email service status", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to get service status: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Test email configuration
     */
    @POST
    @Path("/test")
    public Response testEmailConfiguration(@QueryParam("to") String to) {
        try {
            if (to == null || to.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Test recipient email is required"))
                    .build();
            }
            
            EmailDTO testEmail = new EmailDTO();
            testEmail.setTo(to);
            testEmail.setSubject("MOBA Authorization - Email Test");
            testEmail.setTextContent("This is a test email from MOBA Authorization system.");
            testEmail.setHtmlContent(
                "<html><body>" +
                "<h2>Email Test</h2>" +
                "<p>This is a test email from MOBA Authorization system.</p>" +
                "<p>If you received this email, your email configuration is working correctly.</p>" +
                "</body></html>"
            );
            
            String result = twilioEmailService.sendEmail(testEmail);
            return Response.ok(Map.of("message", result)).build();
        } catch (Exception e) {
            LOGGER.error("Error testing email configuration", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Email configuration test failed: " + e.getMessage()))
                .build();
        }
    }
} 