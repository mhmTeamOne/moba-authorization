package com.mhm.exceptions;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class ValidationException extends RuntimeException {
    
    private final Response.Status status;
    private final String errorCode;
    private final List<ValidationError> validationErrors;
    
    public ValidationException(String message) {
        super(message);
        this.status = Response.Status.BAD_REQUEST;
        this.errorCode = "VALIDATION_ERROR";
        this.validationErrors = null;
    }
    
    public ValidationException(String message, List<ValidationError> validationErrors) {
        super(message);
        this.status = Response.Status.BAD_REQUEST;
        this.errorCode = "VALIDATION_ERROR";
        this.validationErrors = validationErrors;
    }
    
    public ValidationException(String message, Response.Status status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.validationErrors = null;
    }
    
    public ValidationException(String message, Response.Status status, String errorCode, List<ValidationError> validationErrors) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.validationErrors = validationErrors;
    }
    
    public Response.Status getStatus() {
        return status;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
    
    // Static factory methods for common validation errors
    public static ValidationException invalidField(String fieldName, String message) {
        ValidationError error = new ValidationError(fieldName, message, "INVALID_FIELD");
        return new ValidationException("Validation failed for field: " + fieldName, List.of(error));
    }
    
    public static ValidationException requiredField(String fieldName) {
        ValidationError error = new ValidationError(fieldName, "Field is required", "REQUIRED_FIELD");
        return new ValidationException("Required field missing: " + fieldName, List.of(error));
    }
    
    public static ValidationException multipleErrors(List<ValidationError> errors) {
        return new ValidationException("Multiple validation errors occurred", errors);
    }
    
    public static ValidationException invalidFormat(String fieldName, String expectedFormat) {
        ValidationError error = new ValidationError(fieldName, "Invalid format, expected: " + expectedFormat, "INVALID_FORMAT");
        return new ValidationException("Invalid format for field: " + fieldName, List.of(error));
    }
    
    public static ValidationException outOfRange(String fieldName, String range) {
        ValidationError error = new ValidationError(fieldName, "Value out of range: " + range, "OUT_OF_RANGE");
        return new ValidationException("Value out of range for field: " + fieldName, List.of(error));
    }
    
    public static ValidationException tooLong(String fieldName, int maxLength) {
        ValidationError error = new ValidationError(fieldName, "Value too long, max length: " + maxLength, "TOO_LONG");
        return new ValidationException("Value too long for field: " + fieldName, List.of(error));
    }
    
    public static ValidationException tooShort(String fieldName, int minLength) {
        ValidationError error = new ValidationError(fieldName, "Value too short, min length: " + minLength, "TOO_SHORT");
        return new ValidationException("Value too short for field: " + fieldName, List.of(error));
    }
    
    // Inner class for validation error details
    public static class ValidationError {
        private final String field;
        private final String message;
        private final String errorCode;
        private final Object rejectedValue;
        
        public ValidationError(String field, String message, String errorCode) {
            this.field = field;
            this.message = message;
            this.errorCode = errorCode;
            this.rejectedValue = null;
        }
        
        public ValidationError(String field, String message, String errorCode, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.errorCode = errorCode;
            this.rejectedValue = rejectedValue;
        }
        
        public String getField() {
            return field;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getErrorCode() {
            return errorCode;
        }
        
        public Object getRejectedValue() {
            return rejectedValue;
        }
    }
} 