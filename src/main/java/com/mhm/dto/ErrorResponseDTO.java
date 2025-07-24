package com.mhm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mhm.exceptions.ValidationException;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {

    // Getters and setters
    @JsonProperty("error_code")
    private String errorCode;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("status")
    private int status;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("request_id")
    private String requestId;
    
    @JsonProperty("validation_errors")
    private List<ValidationErrorDTO> validationErrors;
    
    @JsonProperty("additional_data")
    private Map<String, Object> additionalData;
    
    @JsonProperty("stack_trace")
    private String stackTrace;
    
    public ErrorResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponseDTO(String errorCode, String message, int status) {
        this();
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
    }
    
    public ErrorResponseDTO(String errorCode, String message, int status, String path) {
        this(errorCode, message, status);
        this.path = path;
    }
    
    public ErrorResponseDTO(String errorCode, String message, int status, String path, String method) {
        this(errorCode, message, status, path);
        this.method = method;
    }

    // Builder pattern
    public static class Builder {
        private final ErrorResponseDTO errorResponse;
        
        public Builder() {
            this.errorResponse = new ErrorResponseDTO();
        }
        
        public Builder errorCode(String errorCode) {
            errorResponse.setErrorCode(errorCode);
            return this;
        }
        
        public Builder message(String message) {
            errorResponse.setMessage(message);
            return this;
        }
        
        public Builder status(int status) {
            errorResponse.setStatus(status);
            return this;
        }
        
        public Builder path(String path) {
            errorResponse.setPath(path);
            return this;
        }
        
        public Builder method(String method) {
            errorResponse.setMethod(method);
            return this;
        }
        
        public Builder requestId(String requestId) {
            errorResponse.setRequestId(requestId);
            return this;
        }
        
        public Builder validationErrors(List<ValidationErrorDTO> validationErrors) {
            errorResponse.setValidationErrors(validationErrors);
            return this;
        }
        
        public Builder additionalData(Map<String, Object> additionalData) {
            errorResponse.setAdditionalData(additionalData);
            return this;
        }
        
        public Builder stackTrace(String stackTrace) {
            errorResponse.setStackTrace(stackTrace);
            return this;
        }
        
        public ErrorResponseDTO build() {
            return errorResponse;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
} 