# Comprehensive Keycloak Admin Client

This document describes the new comprehensive Keycloak Admin Client (`KeycloakAdminClient`) that provides extensive functionality for managing Keycloak operations.

## Overview

The `KeycloakAdminClient` is a reactive REST client that combines all Keycloak operations into a single interface using `io.quarkus.rest.client.reactive`. It replaces the previous separate clients (`KeycloackUserClient` and `UserClient`) and provides many additional features.

## Configuration

Add this configuration to your `application.properties`:

```properties
# Keycloak Admin API client (comprehensive)
quarkus.rest-client.keycloak-admin-api.uri=http://localhost:8080
```

## Features

### 1. Token Operations
- **User Login**: Get user access tokens with username/password
- **Admin Token**: Get admin access tokens for administrative operations
- **Token Refresh**: Refresh expired tokens
- **Token Introspection**: Validate and get token information
- **User Logout**: Logout users and invalidate tokens

### 2. User Management
- **Create Users**: Register new users in Keycloak
- **Get Users**: Retrieve user information by ID or search criteria
- **Update Users**: Modify user attributes
- **Delete Users**: Remove users from Keycloak
- **User Search**: Advanced search with multiple criteria
- **User Count**: Get total number of users
- **Enable/Disable Users**: Control user account status

### 3. Password Management
- **Reset Password**: Reset user passwords with temporary/permanent options
- **Send Verification Email**: Send email verification to users
- **Send Password Reset Email**: Send password reset emails

### 4. Session Management
- **Get User Sessions**: Retrieve active user sessions
- **Logout All Sessions**: Force logout all user sessions

### 5. Group Management
- **Create Groups**: Create new user groups
- **Get Groups**: Retrieve all groups or specific group information
- **Update Groups**: Modify group attributes
- **Delete Groups**: Remove groups
- **User Group Operations**: Add/remove users to/from groups

### 6. Role Management
- **Create Roles**: Create new roles
- **Get Roles**: Retrieve all roles or specific role information
- **Update Roles**: Modify role attributes
- **Delete Roles**: Remove roles
- **User Role Operations**: Add/remove roles to/from users

### 7. Realm Management
- **Get Realm Info**: Retrieve realm configuration
- **Update Realm**: Modify realm settings
- **Event Management**: Retrieve and manage realm events

### 8. Client Management
- **Get Clients**: Retrieve all clients in the realm
- **Create Clients**: Create new client configurations
- **Update Clients**: Modify client settings
- **Delete Clients**: Remove clients
- **Client Sessions**: Get user sessions for specific clients

## Usage Examples

### Basic Token Operations

```java
// Get user token (login)
Uni<Response> userToken = keycloakAdminClient.getUserToken("username", "password");

// Get admin token for administrative operations
Uni<TokenModel> adminToken = keycloakAdminClient.getAdminToken();

// Refresh an expired token
Uni<TokenModel> refreshedToken = keycloakAdminClient.refreshToken("refresh_token_here");

// Logout user
Uni<Response> logoutResponse = keycloakAdminClient.logoutUser("refresh_token_here");
```

### User Management

```java
// Search users with criteria
Uni<List<KeycloackUserDTO>> users = keycloakAdminClient.getUsers(
    "Bearer " + adminToken.getAccess_token(),
    "username", "email@example.com", "firstName", "lastName", 0, 10
);

// Get user by ID
Uni<KeycloackUserDTO> user = keycloakAdminClient.getUserById(
    "Bearer " + adminToken.getAccess_token(), "user-id"
);

// Create new user
Uni<Response> createResponse = keycloakAdminClient.createUser(
    "Bearer " + adminToken.getAccess_token(), userDTO
);

// Reset user password
Map<String, Object> passwordData = Map.of(
    "value", "newPassword123",
    "temporary", false,
    "type", "password"
);
Uni<Response> resetResponse = keycloakAdminClient.resetPassword(
    "Bearer " + adminToken.getAccess_token(), "user-id", passwordData
);
```

### Group Management

```java
// Get all groups
Uni<List<Map<String, Object>>> groups = keycloakAdminClient.getGroups(
    "Bearer " + adminToken.getAccess_token()
);

// Create new group
Map<String, Object> group = Map.of(
    "name", "developers",
    "description", "Development team"
);
Uni<Response> createGroupResponse = keycloakAdminClient.createGroup(
    "Bearer " + adminToken.getAccess_token(), group
);

// Add user to group
Uni<Response> addToGroupResponse = keycloakAdminClient.addUserToGroup(
    "Bearer " + adminToken.getAccess_token(), "user-id", "group-id"
);
```

### Role Management

```java
// Get all roles
Uni<List<Map<String, Object>>> roles = keycloakAdminClient.getRoles(
    "Bearer " + adminToken.getAccess_token()
);

// Create new role
Map<String, Object> role = Map.of(
    "name", "admin",
    "description", "Administrator role"
);
Uni<Response> createRoleResponse = keycloakAdminClient.createRole(
    "Bearer " + adminToken.getAccess_token(), role
);

// Get user roles
Uni<List<Map<String, Object>>> userRoles = keycloakAdminClient.getUserRoles(
    "Bearer " + adminToken.getAccess_token(), "user-id"
);
```

### Session Management

```java
// Get user sessions
Uni<List<Map<String, Object>>> sessions = keycloakAdminClient.getUserSessions(
    "Bearer " + adminToken.getAccess_token(), "user-id"
);

// Logout all user sessions
Uni<Response> logoutAllResponse = keycloakAdminClient.logoutAllUserSessions(
    "Bearer " + adminToken.getAccess_token(), "user-id"
);
```

## UserService Integration

The `UserService` has been updated to use the new client and includes many helper methods:

```java
// Enhanced search capabilities
Uni<List<KeycloackUserDTO>> users = userService.searchUsers(
    "john", "john@example.com", "John", "Doe", 0, 10
);

// Password management
Uni<Response> resetResponse = userService.resetUserPassword(
    "user-id", "newPassword123", false
);

// Session management
Uni<List<Map<String, Object>>> sessions = userService.getUserSessions("user-id");

// Group operations
Uni<Response> addToGroupResponse = userService.addUserToGroup("user-id", "group-id");

// Role operations
Uni<Response> addRoleResponse = userService.addRoleToUser("user-id", "admin");
```

## Migration from Old Clients

The new client replaces the previous separate clients:

**Old way:**
```java
@RestClient KeycloackUserClient keycloackUserClient;
@RestClient UserClient userClient;
```

**New way:**
```java
@RestClient KeycloakAdminClient keycloakAdminClient;
```

Key changes in method signatures:
- `userClient.registerUser(token, user)` → `keycloakAdminClient.createUser(token, user)`
- `userClient.deleteUser(id, token)` → `keycloakAdminClient.deleteUser(token, id)`
- `keycloackUserClient.getUserToken(username, password)` → `keycloakAdminClient.getUserToken(username, password)`

## Benefits

1. **Unified Interface**: All Keycloak operations in one client
2. **Comprehensive Coverage**: Covers all major Keycloak Admin API endpoints
3. **Reactive**: Full reactive programming support with Uni return types
4. **Type Safety**: Proper parameter types and return types
5. **Consistent**: Uniform parameter ordering and naming conventions
6. **Extensible**: Easy to add new Keycloak operations as needed

## Error Handling

All methods return `Uni<Response>` or `Uni<T>` types, allowing for proper reactive error handling:

```java
keycloakAdminClient.createUser(token, user)
    .onItem().transform(response -> {
        if (response.getStatus() == 201) {
            // Success handling
        } else {
            // Error handling
        }
        return response;
    })
    .onFailure().recoverWithUni(throwable -> {
        // Exception handling
        return Uni.createFrom().failure(throwable);
    });
```

This comprehensive client provides all the functionality needed for full Keycloak integration in a reactive Quarkus application. 