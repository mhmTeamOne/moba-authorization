package com.mhm.exceptions;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException {
    
    private final Response.Status status;
    private final String errorCode;
    private final Object additionalData;
    
    public UserException(String message) {
        super(message);
        this.status = Response.Status.BAD_REQUEST;
        this.errorCode = "USER_ERROR";
        this.additionalData = null;
    }
    
    public UserException(String message, Response.Status status) {
        super(message);
        this.status = status;
        this.errorCode = "USER_ERROR";
        this.additionalData = null;
    }
    
    public UserException(String message, Response.Status status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.additionalData = null;
    }
    
    public UserException(String message, Response.Status status, String errorCode, Object additionalData) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.additionalData = additionalData;
    }
    
    public UserException(String message, Throwable cause) {
        super(message, cause);
        this.status = Response.Status.BAD_REQUEST;
        this.errorCode = "USER_ERROR";
        this.additionalData = null;
    }
    
    public UserException(String message, Throwable cause, Response.Status status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.additionalData = null;
    }

    // Static factory methods for common user errors
    public static UserException notFound(Long userId) {
        return new UserException("User not found with ID: " + userId, Response.Status.NOT_FOUND, "USER_NOT_FOUND");
    }
    
    public static UserException notFound(String username) {
        return new UserException("User not found with username: " + username, Response.Status.NOT_FOUND, "USER_NOT_FOUND");
    }
    
    public static UserException alreadyExists(String username) {
        return new UserException("User already exists with username: " + username, Response.Status.CONFLICT, "USER_ALREADY_EXISTS");
    }
    
    public static UserException emailAlreadyExists(String email) {
        return new UserException("User already exists with email: " + email, Response.Status.CONFLICT, "EMAIL_ALREADY_EXISTS");
    }
    
    public static UserException invalidData(String message) {
        return new UserException("Invalid user data: " + message, Response.Status.BAD_REQUEST, "INVALID_USER_DATA");
    }
    
    public static UserException missingRequiredField(String fieldName) {
        return new UserException("Missing required field: " + fieldName, Response.Status.BAD_REQUEST, "MISSING_REQUIRED_FIELD");
    }
    
    public static UserException invalidEmail(String email) {
        return new UserException("Invalid email format: " + email, Response.Status.BAD_REQUEST, "INVALID_EMAIL");
    }
    
    public static UserException passwordTooWeak() {
        return new UserException("Password does not meet security requirements", Response.Status.BAD_REQUEST, "WEAK_PASSWORD");
    }
    
    public static UserException accountDisabled(String username) {
        return new UserException("User account is disabled: " + username, Response.Status.FORBIDDEN, "ACCOUNT_DISABLED");
    }
    
    public static UserException accountLocked(String username) {
        return new UserException("User account is locked: " + username, Response.Status.FORBIDDEN, "ACCOUNT_LOCKED");
    }
    
    public static UserException registrationFailed(String message) {
        return new UserException("User registration failed: " + message, Response.Status.INTERNAL_SERVER_ERROR, "REGISTRATION_FAILED");
    }
    
    public static UserException updateFailed(String message) {
        return new UserException("User update failed: " + message, Response.Status.INTERNAL_SERVER_ERROR, "UPDATE_FAILED");
    }
    
    public static UserException deletionFailed(String message) {
        return new UserException("User deletion failed: " + message, Response.Status.INTERNAL_SERVER_ERROR, "DELETION_FAILED");
    }
} 