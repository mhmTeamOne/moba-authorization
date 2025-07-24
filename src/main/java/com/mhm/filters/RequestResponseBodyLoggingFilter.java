//package com.mhm.filters;
//
//import jakarta.annotation.Priority;
//import jakarta.ws.rs.container.ContainerRequestContext;
//import jakarta.ws.rs.container.ContainerRequestFilter;
//import jakarta.ws.rs.container.ContainerResponseContext;
//import jakarta.ws.rs.container.ContainerResponseFilter;
//import jakarta.ws.rs.ext.Provider;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.StandardCharsets;
//import java.util.Arrays;
//import java.util.List;
//
//@Provider
//@Priority(2000) // Higher priority than main filter
//public class RequestResponseBodyLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(RequestResponseBodyLoggingFilter.class);
//
//    // Paths to exclude from body logging (health checks, etc.)
//    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
//        "/health",
//        "/metrics",
//        "/q/health",
//        "/q/metrics"
//    );
//
//    // Sensitive fields to mask in logs
//    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
//        "password",
//        "token",
//        "secret",
//        "authorization",
//        "credential"
//    );
//
//    // Enable only in development
//    private static final boolean LOGGING_ENABLED = isDebugLoggingEnabled();
//
//    @Override
//    public void filter(ContainerRequestContext requestContext) throws IOException {
//        if (!LOGGING_ENABLED || shouldSkipLogging(requestContext.getUriInfo().getPath())) {
//            return;
//        }
//
//        logRequestBody(requestContext);
//    }
//
//    @Override
//    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
//        if (!LOGGING_ENABLED || shouldSkipLogging(requestContext.getUriInfo().getPath())) {
//            return;
//        }
//
//        logResponseBody(requestContext, responseContext);
//    }
//
//    private void logRequestBody(ContainerRequestContext requestContext) throws IOException {
//        if (requestContext.hasEntity()) {
//            InputStream entityStream = requestContext.getEntityStream();
//            String requestBody = readInputStream(entityStream);
//
//            // Reset the stream for the actual request processing
//            requestContext.setEntityStream(new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8)));
//
//            String maskedBody = maskSensitiveData(requestBody);
//            String method = requestContext.getMethod();
//            String path = requestContext.getUriInfo().getPath();
//
//            LOGGER.debug("Request Body - {} {} | Body: {}", method, path, maskedBody);
//        }
//    }
//
//    private void logResponseBody(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
//        if (responseContext.hasEntity()) {
//            try {
//                Object entity = responseContext.getEntity();
//                if (entity != null) {
//                    String responseBody = entity.toString();
//                    String maskedBody = maskSensitiveData(responseBody);
//                    String method = requestContext.getMethod();
//                    String path = requestContext.getUriInfo().getPath();
//                    int status = responseContext.getStatus();
//
//                    LOGGER.debug("Response Body - {} {} [{}] | Body: {}",
//                               method, path, status, maskedBody);
//                }
//            } catch (Exception e) {
//                LOGGER.warn("Failed to log response body", e);
//            }
//        }
//    }
//
//    private String readInputStream(InputStream inputStream) throws IOException {
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        int nRead;
//        byte[] data = new byte[1024];
//        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
//            buffer.write(data, 0, nRead);
//        }
//        buffer.flush();
//        return buffer.toString(StandardCharsets.UTF_8);
//    }
//
//    private String maskSensitiveData(String jsonData) {
//        if (jsonData == null || jsonData.isEmpty()) {
//            return jsonData;
//        }
//
//        String maskedData = jsonData;
//
//        // Mask sensitive fields in JSON
//        for (String field : SENSITIVE_FIELDS) {
//            // Pattern to match: "field": "value" or "field":"value"
//            String pattern = "\"" + field + "\"\\s*:\\s*\"[^\"]*\"";
//            maskedData = maskedData.replaceAll("(?i)" + pattern, "\"" + field + "\":\"***MASKED***\"");
//        }
//
//        return maskedData;
//    }
//
//    private boolean shouldSkipLogging(String path) {
//        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
//    }
//
//    private static boolean isDebugLoggingEnabled() {
//        // Enable only in development or when explicitly configured
//        String environment = System.getProperty("quarkus.profile", "dev");
//        String debugEnabled = System.getProperty("app.debug.body-logging", "false");
//
//        return "dev".equals(environment) || "true".equals(debugEnabled);
//    }
//}