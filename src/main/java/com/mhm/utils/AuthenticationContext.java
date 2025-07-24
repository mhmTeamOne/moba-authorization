package com.mhm.utils;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;

public class AuthenticationContext {
    
    @Context
    private ContainerRequestContext requestContext;
    
    @Context
    private SecurityContext securityContext;
    
    public String getUserId() {
        if (requestContext != null) {
            return (String) requestContext.getProperty("user.id");
        }
        return null;
    }
    
    public String getUsername() {
        if (requestContext != null) {
            return (String) requestContext.getProperty("user.username");
        }
        return null;
    }
    
    public String getEmail() {
        if (requestContext != null) {
            return (String) requestContext.getProperty("user.email");
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getUserRoles() {
        if (requestContext != null) {
            return (List<String>) requestContext.getProperty("user.roles");
        }
        return null;
    }
    
    public boolean hasRole(String role) {
        List<String> roles = getUserRoles();
        return roles != null && roles.contains(role);
    }
    
    public boolean isAdmin() {
        return hasRole("admin");
    }
    
    public boolean isUser() {
        return hasRole("user");
    }
    
    public String getRequestId() {
        if (requestContext != null) {
            return (String) requestContext.getProperty("request.id");
        }
        return null;
    }
    
    public boolean isAuthenticated() {
        return getUserId() != null;
    }
    
    // Static methods for use without CDI injection
    public static String getUserId(ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty("user.id");
    }
    
    public static String getUsername(ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty("user.username");
    }
    
    public static String getEmail(ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty("user.email");
    }
    
    @SuppressWarnings("unchecked")
    public static List<String> getUserRoles(ContainerRequestContext requestContext) {
        return (List<String>) requestContext.getProperty("user.roles");
    }
    
    public static boolean hasRole(ContainerRequestContext requestContext, String role) {
        List<String> roles = getUserRoles(requestContext);
        return roles != null && roles.contains(role);
    }
} 