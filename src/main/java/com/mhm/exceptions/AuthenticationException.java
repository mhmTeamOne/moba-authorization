package com.mhm.exceptions;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {
    
    private final Response.Status status;
    private final String errorCode;
    private final Object additionalData;
    
    public AuthenticationException(String message) {
        super(message);
        this.status = Response.Status.UNAUTHORIZED;
        this.errorCode = "AUTHENTICATION_ERROR";
        this.additionalData = null;
    }
    
    public AuthenticationException(String message, Response.Status status) {
        super(message);
        this.status = status;
        this.errorCode = "AUTHENTICATION_ERROR";
        this.additionalData = null;
    }
    
    public AuthenticationException(String message, Response.Status status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.additionalData = null;
    }
    
    public AuthenticationException(String message, Response.Status status, String errorCode, Object additionalData) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.additionalData = additionalData;
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.status = Response.Status.UNAUTHORIZED;
        this.errorCode = "AUTHENTICATION_ERROR";
        this.additionalData = null;
    }
    
    public AuthenticationException(String message, Throwable cause, Response.Status status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.additionalData = null;
    }

    // Static factory methods for common authentication errors
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid username or password", Response.Status.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
    
    public static AuthenticationException tokenMissing() {
        return new AuthenticationException("Authentication token is missing", Response.Status.UNAUTHORIZED, "TOKEN_MISSING");
    }
    
    public static AuthenticationException tokenExpired() {
        return new AuthenticationException("Authentication token has expired", Response.Status.UNAUTHORIZED, "TOKEN_EXPIRED");
    }
    
    public static AuthenticationException tokenInvalid() {
        return new AuthenticationException("Authentication token is invalid", Response.Status.UNAUTHORIZED, "TOKEN_INVALID");
    }
    
    public static AuthenticationException tokenMalformed() {
        return new AuthenticationException("Authentication token is malformed", Response.Status.UNAUTHORIZED, "TOKEN_MALFORMED");
    }
    
    public static AuthenticationException userNotFound(String username) {
        return new AuthenticationException("User not found: " + username, Response.Status.UNAUTHORIZED, "USER_NOT_FOUND");
    }
    
    public static AuthenticationException accountDisabled(String username) {
        return new AuthenticationException("Account is disabled: " + username, Response.Status.FORBIDDEN, "ACCOUNT_DISABLED");
    }
    
    public static AuthenticationException accountLocked(String username) {
        return new AuthenticationException("Account is locked: " + username, Response.Status.FORBIDDEN, "ACCOUNT_LOCKED");
    }
    
    public static AuthenticationException accountExpired(String username) {
        return new AuthenticationException("Account has expired: " + username, Response.Status.FORBIDDEN, "ACCOUNT_EXPIRED");
    }
    
    public static AuthenticationException passwordExpired(String username) {
        return new AuthenticationException("Password has expired for user: " + username, Response.Status.FORBIDDEN, "PASSWORD_EXPIRED");
    }
    
    public static AuthenticationException tooManyAttempts(String username) {
        return new AuthenticationException("Too many failed authentication attempts for user: " + username, Response.Status.TOO_MANY_REQUESTS, "TOO_MANY_ATTEMPTS");
    }
    
    public static AuthenticationException sessionExpired() {
        return new AuthenticationException("Session has expired", Response.Status.UNAUTHORIZED, "SESSION_EXPIRED");
    }
    
    public static AuthenticationException sessionInvalid() {
        return new AuthenticationException("Session is invalid", Response.Status.UNAUTHORIZED, "SESSION_INVALID");
    }
    
    public static AuthenticationException authenticationRequired() {
        return new AuthenticationException("Authentication is required", Response.Status.UNAUTHORIZED, "AUTHENTICATION_REQUIRED");
    }
} 