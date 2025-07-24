package com.mhm.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Provider
@Priority(500) // Run before main filter
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Inject
    ObjectMapper objectMapper;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/user/registration",
        "/user/register-with-verification",
        "/user/register-keycloak-first",
        "/user/verify",
        "/user/login",
        "/health",
        "/q/health",
        "/q/metrics",
        "/q/openapi",
        "/q/swagger-ui",
        "/user/admin-token"
    );

    // Admin endpoints that require admin role
    private static final List<String> ADMIN_ENDPOINTS = Arrays.asList(
        "/user/admin-token",
        "/admin/"
    );

    // JWT secret key (should be externalized in production)
    private static final String JWT_SECRET = "mySecretKeyThatShouldBeExternalizedInProductionEnvironment";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            LOGGER.debug("Skipping authentication for public endpoint: {}", path);
            return;
        }

        // Skip authentication for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(method)) {
            return;
        }

        // Extract and validate JWT token
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.warn("Missing or invalid Authorization header for path: {}", path);
            abortWithUnauthorized(requestContext, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            Claims claims = validateToken(token);

            // Check if user has required role for admin endpoints
            if (isAdminEndpoint(path) && !hasAdminRole(claims)) {
                LOGGER.warn("Access denied for path: {} - User lacks admin role", path);
                abortWithForbidden(requestContext, "Access denied - Admin role required");
                return;
            }

            // Add user context to request
            addUserContextToRequest(requestContext, claims);

            LOGGER.debug("Authentication successful for user: {} | path: {}",
                        claims.getSubject(), path);

        } catch (Exception e) {
            LOGGER.warn("Token validation failed for path: {} | Error: {}", path, e.getMessage());
            abortWithUnauthorized(requestContext, "Invalid or expired token");
        }
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private boolean isAdminEndpoint(String path) {
        return ADMIN_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Claims validateToken(String token) throws Exception {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Check if token is expired
            if (claims.getExpiration().before(java.util.Date.from(Instant.now()))) {
                throw new Exception("Token expired");
            }

            return claims;
        } catch (Exception e) {
            throw new Exception("Invalid token: " + e.getMessage());
        }
    }

    private boolean hasAdminRole(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        return roles != null && roles.contains("admin");
    }

    private void addUserContextToRequest(ContainerRequestContext requestContext, Claims claims) {
        // Add user information to request context for use in resource methods
        requestContext.setProperty("user.id", claims.getSubject());
        requestContext.setProperty("user.username", claims.get("username", String.class));
        requestContext.setProperty("user.email", claims.get("email", String.class));
        requestContext.setProperty("user.roles", claims.get("roles", List.class));
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        Response response = createErrorResponse(401, "Unauthorized", message);
        requestContext.abortWith(response);
    }

    private void abortWithForbidden(ContainerRequestContext requestContext, String message) {
        Response response = createErrorResponse(403, "Forbidden", message);
        requestContext.abortWith(response);
    }

    private Response createErrorResponse(int status, String error, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", status);

        return Response.status(status)
                .entity(errorResponse)
                .header("Content-Type", "application/json")
                .build();
    }

    // Utility method to extract user ID from request context (for use in resources)
    public static String getUserId(ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty("user.id");
    }

    // Utility method to extract username from request context
    public static String getUsername(ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty("user.username");
    }

    // Utility method to extract user roles from request context
    @SuppressWarnings("unchecked")
    public static List<String> getUserRoles(ContainerRequestContext requestContext) {
        return (List<String>) requestContext.getProperty("user.roles");
    }
}