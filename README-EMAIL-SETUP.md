# Camel Quarkus Twilio SendGrid Email Integration

This guide explains how to configure and use the Camel Quarkus integration with Twilio SendGrid for sending emails in your MOBA Authorization application.

## 📋 Prerequisites

1. **Twilio SendGrid Account**: Sign up at [https://sendgrid.com/](https://sendgrid.com/)
2. **API Key**: Generate an API key from your SendGrid dashboard
3. **Verified Sender**: Configure a verified sender email in SendGrid

## 🛠️ Setup Instructions

### 1. Get Your SendGrid API Key

1. Log into your SendGrid account
2. Go to Settings > API Keys
3. Create a new API key with "Full Access" permissions
4. Copy the API key (you'll only see it once)

### 2. Update Configuration

Update `src/main/resources/application.properties`:

```properties
# Replace with your actual SendGrid API key
sendgrid.api.key=SG.your_actual_api_key_here
sendgrid.from.email=noreply@yourcompany.com
sendgrid.from.name=Your Company Name
```

### 3. Verify Your Domain/Email

In SendGrid dashboard:
1. Go to Settings > Sender Authentication
2. Either verify a single sender email or authenticate your domain
3. This is required to send emails

## 🚀 Usage Examples

### Simple Email via REST API

```bash
# Send a simple email
curl -X POST http://localhost:8081/email/send \
  -H "Content-Type: application/json" \
  -d '{
    "to": "user@example.com",
    "subject": "Welcome!",
    "textContent": "Welcome to our platform!",
    "htmlContent": "<h1>Welcome!</h1><p>Welcome to our platform!</p>"
  }'
```

### Welcome Email for New Users

```bash
# Send welcome email
curl -X POST "http://localhost:8081/email/welcome?email=user@example.com&name=John"
```

### Password Reset Email

```bash
# Send password reset email
curl -X POST "http://localhost:8081/email/password-reset?email=user@example.com&token=reset_token_123"
```

### Bulk Emails

```bash
# Send bulk emails
curl -X POST http://localhost:8081/email/bulk \
  -H "Content-Type: application/json" \
  -d '[
    {
      "to": "user1@example.com",
      "subject": "Newsletter",
      "textContent": "Monthly newsletter content"
    },
    {
      "to": "user2@example.com",
      "subject": "Newsletter",
      "textContent": "Monthly newsletter content"
    }
  ]'
```

### Email with Attachments

```bash
# Send email with attachment
curl -X POST http://localhost:8081/email/send \
  -H "Content-Type: application/json" \
  -d '{
    "to": "user@example.com",
    "subject": "Document Attached",
    "textContent": "Please find the document attached.",
    "attachments": [
      {
        "filename": "document.pdf",
        "contentType": "application/pdf",
        "content": "base64_encoded_file_content_here"
      }
    ]
  }'
```

## 📊 Available Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/email/send` | Send email via Camel route |
| `POST` | `/email/send-direct` | Send email directly |
| `POST` | `/email/send-smtp` | Send email via SMTP |
| `POST` | `/email/welcome` | Send welcome email |
| `POST` | `/email/password-reset` | Send password reset email |
| `POST` | `/email/bulk` | Send bulk emails |
| `POST` | `/email/template` | Send templated email |
| `POST` | `/email/test` | Test email configuration |
| `GET` | `/email/status` | Get email service status |

## 🔧 Integration in Your Code

### In Your Service Classes

```java
@Inject
TwilioEmailService twilioEmailService;

// Send welcome email after user registration
public void registerUser(UserCreateDTO userCreateDTO) {
    // ... registration logic ...
    
    // Send welcome email
    try {
        twilioEmailService.sendWelcomeEmail(
            userCreateDTO.getEmail(), 
            userCreateDTO.getFirstName()
        );
    } catch (Exception e) {
        // Handle email failure gracefully
        LOGGER.warn("Failed to send welcome email", e);
    }
}
```

### Using Camel Routes

```java
@Inject
ProducerTemplate producerTemplate;

// Send email via Camel route
public void sendEmailViaCamel(EmailDTO emailDTO) {
    producerTemplate.sendBody("direct:send-email", emailDTO);
}
```

## 📋 Email DTO Structure

```java
public class EmailDTO {
    private String to;                    // Required
    private List<String> cc;              // Optional
    private List<String> bcc;             // Optional
    private String subject;               // Required
    private String textContent;           // Required
    private String htmlContent;           // Optional
    private String from;                  // Optional (uses default)
    private String fromName;              // Optional (uses default)
    private List<AttachmentDTO> attachments; // Optional
}
```

## 🛡️ Error Handling

The system includes comprehensive error handling:

```java
// Service automatically handles:
- Invalid API keys
- Network failures
- Invalid email addresses
- SendGrid API errors
- Attachment size limits
```

## 🔍 Testing Your Setup

1. **Test Configuration**:
   ```bash
   curl -X POST "http://localhost:8081/email/test?to=your-email@example.com"
   ```

2. **Check Service Status**:
   ```bash
   curl http://localhost:8081/email/status
   ```

3. **View Logs**:
   ```bash
   # Check application logs for email sending status
   tail -f target/quarkus.log | grep "Email"
   ```

## 📊 Monitoring and Metrics

SendGrid provides detailed analytics:
1. Login to SendGrid Dashboard
2. Go to Analytics > Email Activity
3. Monitor delivery rates, bounces, and opens

## 🔒 Security Best Practices

1. **Environment Variables**: Store API keys in environment variables
   ```bash
   export SENDGRID_API_KEY=your_api_key_here
   ```

2. **Configuration**: Use environment-specific properties
   ```properties
   # application-prod.properties
   sendgrid.api.key=${SENDGRID_API_KEY}
   ```

3. **Rate Limiting**: Implement rate limiting for email endpoints
4. **Validation**: Always validate email addresses before sending

## 🚨 Troubleshooting

### Common Issues

1. **API Key Issues**:
   - Ensure API key has proper permissions
   - Check for typos in configuration

2. **Sender Authentication**:
   - Verify your sender email/domain in SendGrid
   - Check spam folder if emails aren't received

3. **Rate Limits**:
   - Free tier: 100 emails/day
   - Check your SendGrid plan limits

4. **Email Delivery**:
   - Check SendGrid Activity feed
   - Verify recipient email addresses

### Debug Mode

Enable debug logging:
```properties
quarkus.log.category."com.mhm.services.TwilioEmailService".level=DEBUG
```

## 📈 Performance Optimization

1. **Async Processing**: Use Camel routes for non-blocking email sending
2. **Batch Processing**: Use bulk email endpoints for multiple recipients
3. **Connection Pooling**: Configured automatically by SendGrid client
4. **Error Recovery**: Implement retry logic for failed emails

## 🔄 Alternative: Pure SMTP

If you prefer SMTP over API calls:

```properties
# SMTP Configuration
mail.smtp.host=smtp.sendgrid.net
mail.smtp.port=587
mail.smtp.username=apikey
mail.smtp.password=${sendgrid.api.key}
```

Then use the SMTP endpoint:
```bash
curl -X POST http://localhost:8081/email/send-smtp \
  -H "Content-Type: application/json" \
  -d '{...}'
```

## 📚 Additional Resources

- [SendGrid API Documentation](https://docs.sendgrid.com/api-reference)
- [Apache Camel Mail Component](https://camel.apache.org/components/3.20.x/mail-component.html)
- [Quarkus Mailer Guide](https://quarkus.io/guides/mailer)

## 🎯 Next Steps

1. Set up email templates in SendGrid
2. Implement email scheduling
3. Add email tracking and analytics
4. Configure email signatures
5. Set up email automation workflows

---

**Note**: Replace `your_actual_api_key_here` with your real SendGrid API key, and update the sender email to match your verified domain/email. 