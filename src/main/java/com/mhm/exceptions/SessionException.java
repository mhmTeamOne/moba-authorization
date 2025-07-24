package com.mhm.exceptions;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class SessionException extends RuntimeException {
    
    private final Response.Status status;
    private final String errorCode;
    private final Object additionalData;
    
    public SessionException(String message) {
        super(message);
        this.status = Response.Status.INTERNAL_SERVER_ERROR;
        this.errorCode = "SESSION_ERROR";
        this.additionalData = null;
    }
    
    public SessionException(String message, Response.Status status) {
        super(message);
        this.status = status;
        this.errorCode = "SESSION_ERROR";
        this.additionalData = null;
    }
    
    public SessionException(String message, Response.Status status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.additionalData = null;
    }
    
    public SessionException(String message, Response.Status status, String errorCode, Object additionalData) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.additionalData = additionalData;
    }
    
    public SessionException(String message, Throwable cause) {
        super(message, cause);
        this.status = Response.Status.INTERNAL_SERVER_ERROR;
        this.errorCode = "SESSION_ERROR";
        this.additionalData = null;
    }
    
    public SessionException(String message, Throwable cause, Response.Status status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.additionalData = null;
    }

} 