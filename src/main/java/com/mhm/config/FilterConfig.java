package com.mhm.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "app.filter")
public interface FilterConfig {
    
    @WithDefault("true")
    boolean loggingEnabled();
    
    @WithDefault("true")
    boolean securityHeadersEnabled();
    
    @WithDefault("true")
    boolean corsEnabled();
    
    @WithDefault("true")
    boolean performanceMonitoringEnabled();
    
    @WithDefault("*")
    String corsAllowedOrigins();
    
    @WithDefault("GET, POST, PUT, DELETE, OPTIONS")
    String corsAllowedMethods();
    
    @WithDefault("Origin, Content-Type, Accept, Authorization, X-Requested-With")
    String corsAllowedHeaders();
    
    @WithDefault("3600")
    String corsMaxAge();
    
    @WithDefault("X-Request-ID")
    String requestIdHeader();
} 