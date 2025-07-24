package com.mhm.filters;

import com.mhm.services.RateLimitService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Provider
@Priority(1000)
public class EnhancedRequestResponseFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedRequestResponseFilter.class);

    @Inject
    RateLimitService rateLimitService;

    // Headers
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_START_TIME = "request.start.time";
    private static final String REQUEST_ID_KEY = "request.id";
    private static final String RATE_LIMIT_LIMIT = "X-RateLimit-Limit";
    private static final String RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final String RATE_LIMIT_RESET = "X-RateLimit-Reset";

    // Health check and monitoring endpoints to exclude from logging and rate limiting
    private static final List<String> HEALTH_CHECK_ENDPOINTS = Arrays.asList(
        "/health",
        "/q/health",
        "/q/metrics",
        "/q/info",
        "/q/openapi",
        "/q/swagger-ui",
        "/metrics",
        "/actuator/health",
        "/actuator/metrics"
    );

    // Critical endpoints that should have stricter rate limiting
    private static final List<String> CRITICAL_ENDPOINTS = Arrays.asList(
        "/user/login",
        "/user/registration",
        "/user/reset-password"
    );

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        // Skip processing for health check endpoints
        if (isHealthCheckEndpoint(path)) {
            LOGGER.trace("Skipping filter processing for health check endpoint: {}", path);
            return;
        }

        // Generate unique request ID
        String requestId = generateRequestId();
        requestContext.setProperty(REQUEST_ID_KEY, requestId);

        // Set request start time for performance monitoring
        requestContext.setProperty(REQUEST_START_TIME, System.currentTimeMillis());

        // Add request ID to MDC for logging
        MDC.put(REQUEST_ID_KEY, requestId);

        // Get client IP address
        String clientIp = getClientIpAddress(requestContext);

        // Apply rate limiting
        if (shouldApplyRateLimit(path, method)) {
            if (!rateLimitService.isAllowed(clientIp, path, method)) {
                LOGGER.warn("Rate limit exceeded for IP: {} | Path: {} | Method: {}", clientIp, path, method);

                // Add rate limit headers
                RateLimitService.RateLimitInfo rateLimitInfo = rateLimitService.getRateLimitInfo(clientIp, path, method);
                Response rateLimitResponse = Response.status(Response.Status.TOO_MANY_REQUESTS)
                        .entity(createRateLimitErrorResponse())
                        .header(RATE_LIMIT_LIMIT, rateLimitInfo.getLimit())
                        .header(RATE_LIMIT_REMAINING, rateLimitInfo.getRemaining())
                        .header(RATE_LIMIT_RESET, rateLimitInfo.getResetTime())
                        .header(REQUEST_ID_HEADER, requestId)
                        .header("Content-Type", "application/json")
                        .build();

                requestContext.abortWith(rateLimitResponse);
                return;
            }
        }

        // Log incoming request (skip for health checks)
        logIncomingRequest(requestContext, requestId, clientIp);

        // Handle CORS preflight requests
        if ("OPTIONS".equals(method)) {
            handleCorsPreflightRequest(requestContext);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Skip processing for health check endpoints
        if (isHealthCheckEndpoint(path)) {
            return;
        }

        try {
            // Get request ID from context
            String requestId = (String) requestContext.getProperty(REQUEST_ID_KEY);

            // Add request ID to response header
            if (requestId != null) {
                responseContext.getHeaders().add(REQUEST_ID_HEADER, requestId);
            }

            // Add security headers
            addSecurityHeaders(responseContext);

            // Add CORS headers
            addCorsHeaders(responseContext);

            // Add rate limit headers for successful requests
            if (shouldApplyRateLimit(path, requestContext.getMethod())) {
                addRateLimitHeaders(requestContext, responseContext);
            }

            // Calculate and log response time
            Long startTime = (Long) requestContext.getProperty(REQUEST_START_TIME);
            if (startTime != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                logOutgoingResponse(requestContext, responseContext, requestId, responseTime);
            }

        } finally {
            // Clean up MDC
            MDC.clear();
        }
    }

    private boolean isHealthCheckEndpoint(String path) {
        return HEALTH_CHECK_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private boolean isCriticalEndpoint(String path) {
        return CRITICAL_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private boolean shouldApplyRateLimit(String path, String method) {
        // Skip rate limiting for health checks and GET requests to public endpoints
        if (isHealthCheckEndpoint(path)) {
            return false;
        }

        // Apply stricter rate limiting for critical endpoints
        if (isCriticalEndpoint(path)) {
            return true;
        }

        // Apply rate limiting to all POST, PUT, DELETE requests
        return Arrays.asList("POST", "PUT", "DELETE").contains(method);
    }

    private void addRateLimitHeaders(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String clientIp = getClientIpAddress(requestContext);
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        RateLimitService.RateLimitInfo rateLimitInfo = rateLimitService.getRateLimitInfo(clientIp, path, method);

        responseContext.getHeaders().add(RATE_LIMIT_LIMIT, rateLimitInfo.getLimit());
        responseContext.getHeaders().add(RATE_LIMIT_REMAINING, rateLimitInfo.getRemaining());
        responseContext.getHeaders().add(RATE_LIMIT_RESET, rateLimitInfo.getResetTime());
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void logIncomingRequest(ContainerRequestContext requestContext, String requestId, String clientIp) {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        String userAgent = requestContext.getHeaderString("User-Agent");

        LOGGER.info("Incoming request: {} {} | IP: {} | User-Agent: {} | Request-ID: {}",
                   method, path, clientIp, userAgent, requestId);
    }

    private void logOutgoingResponse(ContainerRequestContext requestContext,
                                   ContainerResponseContext responseContext,
                                   String requestId,
                                   long responseTime) {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        int status = responseContext.getStatus();

        if (responseTime > 5000) { // Log slow requests as warnings
            LOGGER.warn("SLOW RESPONSE: {} {} | Status: {} | Time: {}ms | Request-ID: {}",
                       method, path, status, responseTime, requestId);
        } else {
            LOGGER.info("Outgoing response: {} {} | Status: {} | Time: {}ms | Request-ID: {}",
                       method, path, status, responseTime, requestId);
        }
    }

    private void addSecurityHeaders(ContainerResponseContext responseContext) {
        // Security headers
        responseContext.getHeaders().add("X-Content-Type-Options", "nosniff");
        responseContext.getHeaders().add("X-Frame-Options", "DENY");
        responseContext.getHeaders().add("X-XSS-Protection", "1; mode=block");
        responseContext.getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        responseContext.getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");
        responseContext.getHeaders().add("Content-Security-Policy", "default-src 'self'");
    }

    private void addCorsHeaders(ContainerResponseContext responseContext) {
        // CORS headers
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers",
                                        "Origin, Content-Type, Accept, Authorization, X-Requested-With");
        responseContext.getHeaders().add("Access-Control-Max-Age", "3600");
        responseContext.getHeaders().add("Access-Control-Expose-Headers",
                                        REQUEST_ID_HEADER + ", " + RATE_LIMIT_LIMIT + ", " + RATE_LIMIT_REMAINING + ", " + RATE_LIMIT_RESET);
    }

    private void handleCorsPreflightRequest(ContainerRequestContext requestContext) {
        LOGGER.debug("Handling CORS preflight request for: {}", requestContext.getUriInfo().getPath());
    }

    private String getClientIpAddress(ContainerRequestContext requestContext) {
        // Check for X-Forwarded-For header (proxy/load balancer)
        String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // Check for X-Real-IP header
        String xRealIp = requestContext.getHeaderString("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Check for X-Forwarded-For header (alternative format)
        String forwarded = requestContext.getHeaderString("Forwarded");
        if (forwarded != null && forwarded.contains("for=")) {
            String forPart = forwarded.substring(forwarded.indexOf("for=") + 4);
            if (forPart.contains(";")) {
                forPart = forPart.substring(0, forPart.indexOf(";"));
            }
            return forPart.trim();
        }

        // Fallback to remote address
        return "unknown";
    }

    private Object createRateLimitErrorResponse() {
        return new RateLimitErrorResponse(
            "Too Many Requests",
            "Rate limit exceeded. Please try again later.",
            429
        );
    }

    // Inner class for rate limit error response
    public static class RateLimitErrorResponse {
        private final String error;
        private final String message;
        private final int status;

        public RateLimitErrorResponse(String error, String message, int status) {
            this.error = error;
            this.message = message;
            this.status = status;
        }

        public String getError() { return error; }
        public String getMessage() { return message; }
        public int getStatus() { return status; }
    }
}