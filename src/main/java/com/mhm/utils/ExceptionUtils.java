package com.mhm.utils;

import com.mhm.exceptions.*;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class ExceptionUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionUtils.class);
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    // Password validation pattern (at least 8 characters, contains letters and numbers)
    private static final Pattern PASSWORD_PATTERN = 
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$");
    
    private ExceptionUtils() {
        // Utility class
    }
    
    // ===================================
    // USER VALIDATION UTILITIES
    // ===================================
    
    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw ValidationException.requiredField("email");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw ValidationException.invalidFormat("email", "valid email address");
        }
    }
    
    public static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw ValidationException.requiredField("password");
        }
        if (password.length() < 8) {
            throw ValidationException.tooShort("password", 8);
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw ValidationException.invalidFormat("password", 
                    "at least 8 characters with letters and numbers");
        }
    }
    
    public static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw ValidationException.requiredField("username");
        }
        if (username.length() < 3) {
            throw ValidationException.tooShort("username", 3);
        }
        if (username.length() > 50) {
            throw ValidationException.tooLong("username", 50);
        }
    }
    
    public static void validateRequiredField(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw ValidationException.requiredField(fieldName);
        }
    }
    
    public static void validateFieldLength(String fieldName, String value, int minLength, int maxLength) {
        if (value != null) {
            if (value.length() < minLength) {
                throw ValidationException.tooShort(fieldName, minLength);
            }
            if (value.length() > maxLength) {
                throw ValidationException.tooLong(fieldName, maxLength);
            }
        }
    }
    
    // ===================================
    // KEYCLOAK EXCEPTION UTILITIES
    // ===================================
    
    public static KeycloakException handleKeycloakResponse(Response response, String operation) {
        return handleKeycloakResponse(response, operation, null);
    }
    
    public static KeycloakException handleKeycloakResponse(Response response, String operation, String additionalContext) {
        int status = response.getStatus();
        String responseBody = response.readEntity(String.class);
        
        String contextMessage = additionalContext != null ? 
                               " Context: " + additionalContext : "";
        
        return switch (status) {
            case 400 -> new KeycloakException(
                    "Bad request during " + operation + contextMessage,
                    Response.Status.BAD_REQUEST,
                    "KEYCLOAK_BAD_REQUEST",
                    null,
                    responseBody
            );
            case 401 -> KeycloakException.invalidCredentials();
            case 403 -> new KeycloakException(
                    "Forbidden: Insufficient permissions for " + operation + contextMessage,
                    Response.Status.FORBIDDEN,
                    "KEYCLOAK_FORBIDDEN"
            );
            case 404 -> new KeycloakException(
                    "Resource not found during " + operation + contextMessage,
                    Response.Status.NOT_FOUND,
                    "KEYCLOAK_NOT_FOUND"
            );
            case 409 -> new KeycloakException(
                    "Conflict during " + operation + ": Resource already exists" + contextMessage,
                    Response.Status.CONFLICT,
                    "KEYCLOAK_CONFLICT"
            );
            case 500 -> new KeycloakException(
                    "Internal server error during " + operation + contextMessage,
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "KEYCLOAK_SERVER_ERROR"
            );
            default -> new KeycloakException(
                    "Unexpected error during " + operation + ": HTTP " + status + contextMessage,
                    Response.Status.fromStatusCode(status),
                    "KEYCLOAK_UNEXPECTED_ERROR",
                    null,
                    responseBody
            );
        };
    }
    
    public static void validateKeycloakToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw AuthenticationException.tokenMissing();
        }
        if (!token.startsWith("Bearer ")) {
            throw AuthenticationException.tokenMalformed();
        }
    }
    
    // ===================================
    // SESSION EXCEPTION UTILITIES
    // ===================================
    
    public static SessionException sessionNotFound(String sessionId) {
        return new SessionException(
                "Session not found: " + sessionId,
                Response.Status.NOT_FOUND,
                "SESSION_NOT_FOUND"
        );
    }
    
    public static SessionException sessionExpired(String sessionId) {
        return new SessionException(
                "Session expired: " + sessionId,
                Response.Status.UNAUTHORIZED,
                "SESSION_EXPIRED"
        );
    }
    
    public static SessionException sessionInvalid(String sessionId) {
        return new SessionException(
                "Session invalid: " + sessionId,
                Response.Status.UNAUTHORIZED,
                "SESSION_INVALID"
        );
    }
    
    // ===================================
    // GENERAL EXCEPTION UTILITIES
    // ===================================
    
    public static void logAndThrow(Logger logger, RuntimeException exception) {
        logger.error("Exception occurred: {}", exception.getMessage(), exception);
        throw exception;
    }
    
    public static void logAndThrow(Logger logger, String message, RuntimeException exception) {
        logger.error(message + ": {}", exception.getMessage(), exception);
        throw exception;
    }
    
    public static RuntimeException wrapException(Exception exception, String message) {
        if (exception instanceof RuntimeException) {
            return (RuntimeException) exception;
        }
        return new RuntimeException(message, exception);
    }
    
    public static String extractErrorMessage(Exception exception) {
        if (exception instanceof KeycloakException) {
            return ((KeycloakException) exception).getErrorCode() + ": " + exception.getMessage();
        }
        if (exception instanceof UserException) {
            return ((UserException) exception).getErrorCode() + ": " + exception.getMessage();
        }
        if (exception instanceof ValidationException) {
            return ((ValidationException) exception).getErrorCode() + ": " + exception.getMessage();
        }
        if (exception instanceof AuthenticationException) {
            return ((AuthenticationException) exception).getErrorCode() + ": " + exception.getMessage();
        }
        if (exception instanceof SessionException) {
            return ((SessionException) exception).getErrorCode() + ": " + exception.getMessage();
        }
        return exception.getMessage();
    }
    
    // ===================================
    // AUTHENTICATION UTILITIES
    // ===================================
    
    public static void validateAuthenticationToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw AuthenticationException.tokenMissing();
        }
        
        if (!token.startsWith("Bearer ")) {
            throw AuthenticationException.tokenMalformed();
        }
        
        String actualToken = token.substring(7); // Remove "Bearer " prefix
        if (actualToken.trim().isEmpty()) {
            throw AuthenticationException.tokenMissing();
        }
    }
    
    public static void validateUserPermission(String userId, String requiredPermission) {
        // This is a placeholder - implement your actual permission checking logic
        if (userId == null || userId.trim().isEmpty()) {
            throw AuthenticationException.userNotFound("unknown");
        }
        
        // Example permission check logic
        if (requiredPermission != null && !hasPermission(userId, requiredPermission)) {
            throw new AuthenticationException(
                    "User " + userId + " lacks required permission: " + requiredPermission,
                    Response.Status.FORBIDDEN,
                    "INSUFFICIENT_PERMISSIONS"
            );
        }
    }
    
    private static boolean hasPermission(String userId, String permission) {
        // Placeholder implementation - replace with actual permission checking
        return true;
    }
    
    // ===================================
    // BUSINESS LOGIC UTILITIES
    // ===================================
    
    public static void validateBusinessRule(boolean condition, String message) {
        if (!condition) {
            throw new UserException(message, Response.Status.BAD_REQUEST, "BUSINESS_RULE_VIOLATION");
        }
    }
    
    public static void validateBusinessRule(boolean condition, String message, String errorCode) {
        if (!condition) {
            throw new UserException(message, Response.Status.BAD_REQUEST, errorCode);
        }
    }
    
    public static void validateResourceExists(Object resource, String resourceType, String resourceId) {
        if (resource == null) {
            throw new UserException(
                    resourceType + " not found: " + resourceId,
                    Response.Status.NOT_FOUND,
                    "RESOURCE_NOT_FOUND"
            );
        }
    }
    
    public static void validateResourceOwnership(String userId, String resourceOwnerId, String resourceType) {
        if (!userId.equals(resourceOwnerId)) {
            throw new AuthenticationException(
                    "User " + userId + " does not own " + resourceType,
                    Response.Status.FORBIDDEN,
                    "RESOURCE_ACCESS_DENIED"
            );
        }
    }
} 