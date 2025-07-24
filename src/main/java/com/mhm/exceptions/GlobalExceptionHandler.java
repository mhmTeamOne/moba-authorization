package com.mhm.exceptions;

import com.mhm.dto.ErrorResponseDTO;
import com.mhm.dto.ValidationErrorDTO;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @Context
    UriInfo uriInfo;
    
    @ConfigProperty(name = "app.debug.include-stack-trace", defaultValue = "false")
    boolean includeStackTrace;
    
    @ConfigProperty(name = "app.debug.detailed-error-messages", defaultValue = "false")
    boolean detailedErrorMessages;
    
    @Override
    public Response toResponse(Exception exception) {
        String requestId = UUID.randomUUID().toString();
        String path = uriInfo != null ? uriInfo.getPath() : "unknown";
        String method = uriInfo != null && uriInfo.getRequestUri() != null ? 
                       uriInfo.getRequestUri().getQuery() : "unknown";
        
        ErrorResponseDTO errorResponse;
        
        // Handle custom exceptions
        if (exception instanceof SessionException) {
            errorResponse = handleSessionException((SessionException) exception, path, method, requestId);
        } else if (exception instanceof KeycloakException) {
            errorResponse = handleKeycloakException((KeycloakException) exception, path, method, requestId);
        } else if (exception instanceof UserException) {
            errorResponse = handleUserException((UserException) exception, path, method, requestId);
        } else if (exception instanceof ValidationException) {
            errorResponse = handleValidationException((ValidationException) exception, path, method, requestId);
        } else if (exception instanceof AuthenticationException) {
            errorResponse = handleAuthenticationException((AuthenticationException) exception, path, method, requestId);
        } else if (exception instanceof WebApplicationException) {
            errorResponse = handleWebApplicationException((WebApplicationException) exception, path, method, requestId);
        } else {
            errorResponse = handleGeneralException(exception, path, method, requestId);
        }
        
        // Add stack trace if enabled
        if (includeStackTrace) {
            errorResponse.setStackTrace(getStackTraceAsString(exception));
        }
        
        return Response.status(errorResponse.getStatus())
                .entity(errorResponse)
                .build();
    }
    
    private ErrorResponseDTO handleSessionException(SessionException exception, String path, String method, String requestId) {
        LOGGER.error("Session exception: {} - RequestId: {}", exception.getMessage(), requestId, exception);
        
        return ErrorResponseDTO.builder()
                .errorCode(exception.getErrorCode())
                .message(exception.getMessage())
                .status(exception.getStatus().getStatusCode())
                .path(path)
                .method(method)
                .requestId(requestId)
                .additionalData(exception.getAdditionalData() != null ? 
                               (java.util.Map<String, Object>) exception.getAdditionalData() : null)
                .build();
    }
    
    private ErrorResponseDTO handleKeycloakException(KeycloakException exception, String path, String method, String requestId) {
        LOGGER.error("Keycloak exception: {} - RequestId: {}", exception.getMessage(), requestId, exception);
        
        java.util.Map<String, Object> additionalData = new java.util.HashMap<>();
        if (exception.getKeycloakErrorCode() != null) {
            additionalData.put("keycloak_error_code", exception.getKeycloakErrorCode());
        }
        if (exception.getAdditionalData() != null) {
            additionalData.put("additional_data", exception.getAdditionalData());
        }
        
        return ErrorResponseDTO.builder()
                .errorCode(exception.getErrorCode())
                .message(exception.getMessage())
                .status(exception.getStatus().getStatusCode())
                .path(path)
                .method(method)
                .requestId(requestId)
                .additionalData(additionalData.isEmpty() ? null : additionalData)
                .build();
    }
    
    private ErrorResponseDTO handleUserException(UserException exception, String path, String method, String requestId) {
        LOGGER.error("User exception: {} - RequestId: {}", exception.getMessage(), requestId, exception);
        
        return ErrorResponseDTO.builder()
                .errorCode(exception.getErrorCode())
                .message(exception.getMessage())
                .status(exception.getStatus().getStatusCode())
                .path(path)
                .method(method)
                .requestId(requestId)
                .additionalData(exception.getAdditionalData() != null ? 
                               (java.util.Map<String, Object>) exception.getAdditionalData() : null)
                .build();
    }
    
    private ErrorResponseDTO handleValidationException(ValidationException exception, String path, String method, String requestId) {
        LOGGER.warn("Validation exception: {} - RequestId: {}", exception.getMessage(), requestId);
        
        List<ValidationErrorDTO> validationErrors = null;
        if (exception.getValidationErrors() != null) {
            validationErrors = exception.getValidationErrors().stream()
                    .map(error -> new ValidationErrorDTO(
                            error.getField(),
                            error.getMessage(),
                            error.getErrorCode(),
                            error.getRejectedValue()))
                    .collect(Collectors.toList());
        }
        
        return ErrorResponseDTO.builder()
                .errorCode(exception.getErrorCode())
                .message(exception.getMessage())
                .status(exception.getStatus().getStatusCode())
                .path(path)
                .method(method)
                .requestId(requestId)
                .validationErrors(validationErrors)
                .build();
    }
    
    private ErrorResponseDTO handleAuthenticationException(AuthenticationException exception, String path, String method, String requestId) {
        LOGGER.warn("Authentication exception: {} - RequestId: {}", exception.getMessage(), requestId);
        
        return ErrorResponseDTO.builder()
                .errorCode(exception.getErrorCode())
                .message(exception.getMessage())
                .status(exception.getStatus().getStatusCode())
                .path(path)
                .method(method)
                .requestId(requestId)
                .additionalData(exception.getAdditionalData() != null ? 
                               (java.util.Map<String, Object>) exception.getAdditionalData() : null)
                .build();
    }
    
    private ErrorResponseDTO handleWebApplicationException(WebApplicationException exception, String path, String method, String requestId) {
        LOGGER.warn("Web application exception: {} - Status: {} - RequestId: {}", 
                   exception.getMessage(), exception.getResponse().getStatus(), requestId);
        
        String errorCode = "HTTP_" + exception.getResponse().getStatus();
        String message = exception.getMessage();
        
        if (message == null || message.trim().isEmpty()) {
            message = getHttpStatusMessage(exception.getResponse().getStatus());
        }
        
        return ErrorResponseDTO.builder()
                .errorCode(errorCode)
                .message(message)
                .status(exception.getResponse().getStatus())
                .path(path)
                .method(method)
                .requestId(requestId)
                .build();
    }
    
    private ErrorResponseDTO handleGeneralException(Exception exception, String path, String method, String requestId) {
        LOGGER.error("Unhandled exception: {} - RequestId: {}", exception.getMessage(), requestId, exception);
        
        String message = detailedErrorMessages ? 
                        exception.getMessage() : 
                        "An unexpected error occurred";
        
        return ErrorResponseDTO.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message(message)
                .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .path(path)
                .method(method)
                .requestId(requestId)
                .build();
    }
    
    private String getHttpStatusMessage(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 409 -> "Conflict";
            case 422 -> "Unprocessable Entity";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            default -> "HTTP Error " + status;
        };
    }
    
    private String getStackTraceAsString(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }
} 