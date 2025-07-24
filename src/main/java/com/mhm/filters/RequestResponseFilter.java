package com.mhm.filters;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

@Provider
@Priority(1000)
public class RequestResponseFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestResponseFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_START_TIME = "request.start.time";
    private static final String REQUEST_ID_KEY = "request.id";
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Generate unique request ID
        String requestId = generateRequestId();
        requestContext.setProperty(REQUEST_ID_KEY, requestId);
        
        // Set request start time for performance monitoring
        requestContext.setProperty(REQUEST_START_TIME, System.currentTimeMillis());
        
        // Add request ID to MDC for logging
        MDC.put(REQUEST_ID_KEY, requestId);
        
        // Log incoming request
        logIncomingRequest(requestContext, requestId);
        
        // Handle CORS preflight requests
        if ("OPTIONS".equals(requestContext.getMethod())) {
            handleCorsPreflightRequest(requestContext);
        }
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
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
    
    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    private void logIncomingRequest(ContainerRequestContext requestContext, String requestId) {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        String remoteAddr = getClientIpAddress(requestContext);
        String userAgent = requestContext.getHeaderString("User-Agent");
        
        LOGGER.info("Incoming request: {} {} | IP: {} | User-Agent: {} | Request-ID: {}", 
                   method, path, remoteAddr, userAgent, requestId);
    }
    
    private void logOutgoingResponse(ContainerRequestContext requestContext, 
                                   ContainerResponseContext responseContext, 
                                   String requestId, 
                                   long responseTime) {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        int status = responseContext.getStatus();
        
        LOGGER.info("Outgoing response: {} {} | Status: {} | Time: {}ms | Request-ID: {}", 
                   method, path, status, responseTime, requestId);
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
        responseContext.getHeaders().add("Access-Control-Expose-Headers", REQUEST_ID_HEADER);
    }
    
    private void handleCorsPreflightRequest(ContainerRequestContext requestContext) {
        // For OPTIONS requests, we'll let the response filter handle the headers
        // and return early to avoid processing the request further
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
        
        // Fallback to remote address (may not be available in all environments)
        return "unknown";
    }
} 