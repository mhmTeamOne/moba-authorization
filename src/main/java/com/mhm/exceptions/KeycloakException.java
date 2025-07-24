package com.mhm.exceptions;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class KeycloakException extends RuntimeException {
    
    private final Response.Status status;
    private final String errorCode;
    private final String keycloakErrorCode;
    private final Object additionalData;
    
    public KeycloakException(String message) {
        super(message);
        this.status = Response.Status.INTERNAL_SERVER_ERROR;
        this.errorCode = "KEYCLOAK_ERROR";
        this.keycloakErrorCode = null;
        this.additionalData = null;
    }
    
    public KeycloakException(String message, Response.Status status) {
        super(message);
        this.status = status;
        this.errorCode = "KEYCLOAK_ERROR";
        this.keycloakErrorCode = null;
        this.additionalData = null;
    }
    
    public KeycloakException(String message, Response.Status status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.keycloakErrorCode = null;
        this.additionalData = null;
    }
    
    public KeycloakException(String message, Response.Status status, String errorCode, String keycloakErrorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.keycloakErrorCode = keycloakErrorCode;
        this.additionalData = null;
    }
    
    public KeycloakException(String message, Response.Status status, String errorCode, String keycloakErrorCode, Object additionalData) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.keycloakErrorCode = keycloakErrorCode;
        this.additionalData = additionalData;
    }
    
    public KeycloakException(String message, Throwable cause) {
        super(message, cause);
        this.status = Response.Status.INTERNAL_SERVER_ERROR;
        this.errorCode = "KEYCLOAK_ERROR";
        this.keycloakErrorCode = null;
        this.additionalData = null;
    }
    
    public KeycloakException(String message, Throwable cause, Response.Status status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.keycloakErrorCode = null;
        this.additionalData = null;
    }

    // Static factory methods for common Keycloak errors
    public static KeycloakException userNotFound(String userId) {
        return new KeycloakException("User not found: " + userId, Response.Status.NOT_FOUND, "USER_NOT_FOUND");
    }
    
    public static KeycloakException userAlreadyExists(String username) {
        return new KeycloakException("User already exists: " + username, Response.Status.CONFLICT, "USER_ALREADY_EXISTS");
    }
    
    public static KeycloakException invalidCredentials() {
        return new KeycloakException("Invalid credentials", Response.Status.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
    
    public static KeycloakException tokenExpired() {
        return new KeycloakException("Token has expired", Response.Status.UNAUTHORIZED, "TOKEN_EXPIRED");
    }
    
    public static KeycloakException tokenInvalid() {
        return new KeycloakException("Token is invalid", Response.Status.UNAUTHORIZED, "TOKEN_INVALID");
    }
    
    public static KeycloakException adminTokenFailed() {
        return new KeycloakException("Failed to obtain admin token", Response.Status.INTERNAL_SERVER_ERROR, "ADMIN_TOKEN_FAILED");
    }
    
    public static KeycloakException groupNotFound(String groupId) {
        return new KeycloakException("Group not found: " + groupId, Response.Status.NOT_FOUND, "GROUP_NOT_FOUND");
    }
    
    public static KeycloakException roleNotFound(String roleName) {
        return new KeycloakException("Role not found: " + roleName, Response.Status.NOT_FOUND, "ROLE_NOT_FOUND");
    }
    
    public static KeycloakException communicationError(String message, Throwable cause) {
        return new KeycloakException("Keycloak communication error: " + message, cause, Response.Status.INTERNAL_SERVER_ERROR, "KEYCLOAK_COMMUNICATION_ERROR");
    }
} 