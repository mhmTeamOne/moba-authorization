package com.mhm.services;

import com.mhm.exceptions.GlobalExceptionHandler;
import com.mhm.exceptions.KeycloakException;
import com.mhm.exceptions.UserException;
import com.mhm.exceptions.ValidationException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Supplier;

@ApplicationScoped
public class ErrorHandlingService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlingService.class);
    
    @Inject
    GlobalExceptionHandler globalExceptionHandler;
    
    /**
     * Execute a risky operation and handle any exceptions using GlobalExceptionHandler
     */
    public <T> Uni<Response> executeWithGlobalErrorHandling(Supplier<T> operation, String operationName) {
        try {
            T result = operation.get();
            return Uni.createFrom().item(Response.ok(result).build());
        } catch (Exception e) {
            LOGGER.error("Error in operation {}: {}", operationName, e.getMessage());
            Response errorResponse = globalExceptionHandler.toResponse(e);
            return Uni.createFrom().item(errorResponse);
        }
    }
    
    /**
     * Handle specific exception types with custom logic, then use GlobalExceptionHandler
     */
    public Uni<Response> handleSpecificExceptions(Exception exception) {
        // Add custom logic for specific exceptions
        if (exception instanceof KeycloakException keycloakException) {
            // Custom logging for Keycloak exceptions
            LOGGER.error("Keycloak integration failed: {}", keycloakException.getMessage());
            
            // You can add custom logic here (notifications, metrics, etc.)
            // sendNotificationToAdmins(keycloakException);
            
        } else if (exception instanceof UserException userException) {
            // Custom logic for user exceptions
            LOGGER.warn("User operation failed: {}", userException.getMessage());
        }
        
        // Use GlobalExceptionHandler to format the response
        Response errorResponse = globalExceptionHandler.toResponse(exception);
        return Uni.createFrom().item(errorResponse);
    }
    
    /**
     * Convert business logic exceptions to standardized error responses
     */
    public Response convertToStandardError(String message, String errorCode, Response.Status status) {
        Exception businessException = switch (errorCode) {
            case "KEYCLOAK_ERROR" -> new KeycloakException(message, status, errorCode);
            case "USER_ERROR" -> new UserException(message, status, errorCode);
            case "VALIDATION_ERROR" -> new ValidationException(message, status, errorCode);
            default -> new RuntimeException(message);
        };
        
        return globalExceptionHandler.toResponse(businessException);
    }
    
    /**
     * Handle async operations with error handling
     */
    public Uni<Response> handleAsyncOperation(Uni<String> asyncOperation) {
        return asyncOperation
            .onItem().transform(result -> Response.ok(Map.of("result", result)).build())
            .onFailure().recoverWithUni(throwable -> {
                Response errorResponse = globalExceptionHandler.toResponse((Exception) throwable);
                return Uni.createFrom().item(errorResponse);
            });
    }
} 