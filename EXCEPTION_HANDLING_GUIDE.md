# Comprehensive Exception Handling System

This document describes the comprehensive exception handling system designed for your reactive Quarkus application with Keycloak integration.

## Overview

The exception handling system provides a robust, standardized way to handle all types of errors in your application. It includes custom exception classes, utilities for validation and error handling, and a global exception handler that produces consistent error responses.

## Architecture

### Components

1. **Custom Exception Classes**: Specific exception types for different error scenarios
2. **Error Response DTOs**: Standardized error response structures
3. **Global Exception Handler**: Centralized exception processing
4. **Exception Utilities**: Helper methods for common validation and error handling tasks

### Exception Hierarchy

```
RuntimeException
├── SessionException          - Session-related errors
├── KeycloakException        - Keycloak integration errors
├── UserException            - User management errors
├── ValidationException      - Input validation errors
└── AuthenticationException  - Authentication/authorization errors
```

## Exception Classes

### 1. SessionException
Handles session-related errors including session expiration, invalid sessions, and session management issues.

**Factory Methods:**
- `SessionException.sessionNotFound(sessionId)`
- `SessionException.sessionExpired(sessionId)`
- `SessionException.sessionInvalid(sessionId)`

**Example Usage:**
```java
if (session == null) {
    throw ExceptionUtils.sessionNotFound(sessionId);
}
```

### 2. KeycloakException
Handles Keycloak-specific errors including API communication issues, authentication failures, and user management errors.

**Factory Methods:**
- `KeycloakException.userNotFound(userId)`
- `KeycloakException.userAlreadyExists(username)`
- `KeycloakException.invalidCredentials()`
- `KeycloakException.tokenExpired()`
- `KeycloakException.adminTokenFailed()`

**Example Usage:**
```java
if (response.getStatus() == 409) {
    throw KeycloakException.userAlreadyExists(username);
}
```

### 3. UserException
Handles user-related business logic errors including user not found, validation failures, and account issues.

**Factory Methods:**
- `UserException.notFound(userId)`
- `UserException.alreadyExists(username)`
- `UserException.emailAlreadyExists(email)`
- `UserException.accountDisabled(username)`
- `UserException.registrationFailed(message)`

**Example Usage:**
```java
if (user == null) {
    throw UserException.notFound(userId);
}
```

### 4. ValidationException
Handles input validation errors with detailed field-level error information.

**Factory Methods:**
- `ValidationException.requiredField(fieldName)`
- `ValidationException.invalidFormat(fieldName, expectedFormat)`
- `ValidationException.tooLong(fieldName, maxLength)`
- `ValidationException.tooShort(fieldName, minLength)`

**Example Usage:**
```java
if (email == null || email.isEmpty()) {
    throw ValidationException.requiredField("email");
}
```

### 5. AuthenticationException
Handles authentication and authorization errors including token issues, permission failures, and access control.

**Factory Methods:**
- `AuthenticationException.invalidCredentials()`
- `AuthenticationException.tokenMissing()`
- `AuthenticationException.tokenExpired()`
- `AuthenticationException.accountDisabled(username)`

**Example Usage:**
```java
if (token == null) {
    throw AuthenticationException.tokenMissing();
}
```

## Error Response Structure

All exceptions are converted to a standardized JSON response:

```json
{
  "error_code": "USER_NOT_FOUND",
  "message": "User not found with ID: 123",
  "status": 404,
  "timestamp": "2023-12-07T10:30:00",
  "path": "/user/123",
  "method": "GET",
  "request_id": "uuid-here",
  "validation_errors": [...],
  "additional_data": {...},
  "stack_trace": "..." // Only in debug mode
}
```

### Fields Description

- **error_code**: Machine-readable error identifier
- **message**: Human-readable error description
- **status**: HTTP status code
- **timestamp**: When the error occurred
- **path**: The requested endpoint
- **method**: HTTP method used
- **request_id**: Unique identifier for request tracing
- **validation_errors**: Array of field-specific validation errors
- **additional_data**: Context-specific error information
- **stack_trace**: Full stack trace (debug mode only)

## Exception Utilities

The `ExceptionUtils` class provides helper methods for common validation and error handling tasks:

### User Validation
```java
// Email validation
ExceptionUtils.validateEmail(email);

// Password validation
ExceptionUtils.validatePassword(password);

// Username validation
ExceptionUtils.validateUsername(username);

// Required field validation
ExceptionUtils.validateRequiredField("firstName", firstName);

// Field length validation
ExceptionUtils.validateFieldLength("description", description, 10, 500);
```

### Authentication Validation
```java
// Token validation
ExceptionUtils.validateAuthenticationToken(token);

// Permission validation
ExceptionUtils.validateUserPermission(userId, "READ_USERS");
```

### Business Logic Validation
```java
// Business rule validation
ExceptionUtils.validateBusinessRule(user.getAge() >= 18, "User must be at least 18 years old");

// Resource existence validation
ExceptionUtils.validateResourceExists(user, "User", userId);

// Resource ownership validation
ExceptionUtils.validateResourceOwnership(currentUserId, resourceOwnerId, "Document");
```

### Keycloak Response Handling
```java
// Handle Keycloak API responses
if (response.getStatus() != 200) {
    throw ExceptionUtils.handleKeycloakResponse(response, "user creation");
}
```

## Configuration

Add these properties to your `application.properties`:

```properties
# Debug Configuration
app.debug.include-stack-trace=false
app.debug.detailed-error-messages=false
```

### Configuration Options

- **include-stack-trace**: Include full stack traces in error responses (development only)
- **detailed-error-messages**: Show detailed error messages vs generic messages

## Usage Examples

### 1. User Registration with Validation

```java
public Uni<Response> registerUser(UserCreateDTO userCreateDTO) {
    // Validate input
    ExceptionUtils.validateRequiredField("username", userCreateDTO.getUsername());
    ExceptionUtils.validateEmail(userCreateDTO.getEmail());
    ExceptionUtils.validatePassword(userCreateDTO.getPassword());
    
    // Check for existing users
    return userRepository.findByUsername(userCreateDTO.getUsername())
        .onItem().ifNotNull().failWith(() -> UserException.alreadyExists(userCreateDTO.getUsername()))
        .onItem().ifNull().switchTo(() -> userRepository.findByEmail(userCreateDTO.getEmail()))
        .onItem().ifNotNull().failWith(() -> UserException.emailAlreadyExists(userCreateDTO.getEmail()))
        .onItem().ifNull().switchTo(() -> {
            // Create user
            UserEntity user = userMapper.toEntity(userCreateDTO);
            return userRepository.persist(user);
        })
        .onItem().transform(user -> {
            UserResponseDTO response = userMapper.toResponseDTO(user);
            return Response.status(Response.Status.CREATED).entity(response).build();
        });
}
```

### 2. Keycloak Integration with Error Handling

```java
public Uni<Response> createKeycloakUser(String adminToken, KeycloackUserDTO userDTO) {
    ExceptionUtils.validateKeycloakToken(adminToken);
    
    return keycloakAdminClient.createUser(adminToken, userDTO)
        .onItem().transform(response -> {
            if (response.getStatus() == 201) {
                return Response.status(Response.Status.CREATED).build();
            } else {
                throw ExceptionUtils.handleKeycloakResponse(response, "user creation");
            }
        })
        .onFailure().recoverWithUni(failure -> {
            if (failure instanceof KeycloakException) {
                // Let the global handler deal with it
                return Uni.createFrom().failure(failure);
            }
            // Wrap unexpected errors
            return Uni.createFrom().failure(
                KeycloakException.communicationError("Failed to create user", failure)
            );
        });
}
```

### 3. Authentication with Custom Error Handling

```java
public Uni<Response> authenticateUser(UserLoginDTO loginDTO) {
    ExceptionUtils.validateRequiredField("username", loginDTO.getUserName());
    ExceptionUtils.validateRequiredField("password", loginDTO.getPassword());
    
    return keycloakAdminClient.getUserToken(loginDTO.getUserName(), loginDTO.getPassword())
        .onItem().transform(response -> {
            if (response.getStatus() == 200) {
                return response;
            } else if (response.getStatus() == 401) {
                throw AuthenticationException.invalidCredentials();
            } else {
                throw ExceptionUtils.handleKeycloakResponse(response, "authentication");
            }
        })
        .onFailure().recoverWithUni(failure -> {
            // Additional error handling logic
            return Uni.createFrom().failure(failure);
        });
}
```

## Best Practices

### 1. Use Specific Exception Types
```java
// Good
throw UserException.notFound(userId);

// Bad
throw new RuntimeException("User not found");
```

### 2. Provide Meaningful Error Messages
```java
// Good
throw ValidationException.invalidFormat("email", "valid email address");

// Bad
throw new ValidationException("Invalid email");
```

### 3. Use Factory Methods
```java
// Good
throw KeycloakException.userAlreadyExists(username);

// Bad
throw new KeycloakException("User already exists", Response.Status.CONFLICT, "USER_EXISTS");
```

### 4. Validate Early and Often
```java
// Validate at the beginning of methods
ExceptionUtils.validateRequiredField("userId", userId);
ExceptionUtils.validateAuthenticationToken(token);
```

### 5. Handle Keycloak Responses Consistently
```java
// Use the utility method for consistent error handling
if (response.getStatus() != 200) {
    throw ExceptionUtils.handleKeycloakResponse(response, "operation name");
}
```

## Error Codes Reference

### User Errors
- `USER_NOT_FOUND`: User does not exist
- `USER_ALREADY_EXISTS`: Username already taken
- `EMAIL_ALREADY_EXISTS`: Email already registered
- `INVALID_USER_DATA`: Invalid user information
- `ACCOUNT_DISABLED`: User account is disabled

### Authentication Errors
- `INVALID_CREDENTIALS`: Wrong username/password
- `TOKEN_MISSING`: Authentication token not provided
- `TOKEN_EXPIRED`: Authentication token expired
- `TOKEN_INVALID`: Authentication token is invalid
- `INSUFFICIENT_PERMISSIONS`: User lacks required permissions

### Validation Errors
- `REQUIRED_FIELD`: Required field is missing
- `INVALID_FORMAT`: Field format is incorrect
- `TOO_LONG`: Field value exceeds maximum length
- `TOO_SHORT`: Field value below minimum length

### Keycloak Errors
- `KEYCLOAK_COMMUNICATION_ERROR`: Communication with Keycloak failed
- `KEYCLOAK_CONFLICT`: Resource already exists in Keycloak
- `KEYCLOAK_NOT_FOUND`: Resource not found in Keycloak
- `ADMIN_TOKEN_FAILED`: Failed to obtain admin token

### Session Errors
- `SESSION_NOT_FOUND`: Session does not exist
- `SESSION_EXPIRED`: Session has expired
- `SESSION_INVALID`: Session is invalid

This comprehensive exception handling system provides a robust foundation for error management in your reactive Quarkus application with Keycloak integration. 