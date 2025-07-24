package com.mhm.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class RateLimitService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitService.class);
    
    // Configuration
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;
    private static final int CLEANUP_INTERVAL_MINUTES = 5;
    
    // Storage for request counts
    private final ConcurrentHashMap<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    private LocalDateTime lastCleanup = LocalDateTime.now();
    
    public boolean isAllowed(String clientIp) {
        return isAllowed(clientIp, "/", "GET");
    }
    
    public boolean isAllowed(String clientIp, String path, String method) {
        cleanupOldEntries();
        
        String key = clientIp + ":" + path + ":" + method;
        RequestCounter counter = requestCounters.computeIfAbsent(key, k -> new RequestCounter());
        
        boolean allowed = counter.tryAcquire();
        
        if (!allowed) {
            LOGGER.warn("Rate limit exceeded for IP: {} | Path: {} | Method: {} | Current count: {}", 
                       clientIp, path, method, counter.getRequestCount());
        }
        
        return allowed;
    }
    
    public RateLimitInfo getRateLimitInfo(String clientIp, String path, String method) {
        String key = clientIp + ":" + path + ":" + method;
        RequestCounter counter = requestCounters.get(key);
        
        if (counter == null) {
            return new RateLimitInfo(MAX_REQUESTS_PER_MINUTE, MAX_REQUESTS_PER_MINUTE, 0);
        }
        
        int currentCount = counter.getRequestCount();
        int remaining = Math.max(0, MAX_REQUESTS_PER_MINUTE - currentCount);
        
        return new RateLimitInfo(MAX_REQUESTS_PER_MINUTE, remaining, counter.getResetTime());
    }
    
    private void cleanupOldEntries() {
        LocalDateTime now = LocalDateTime.now();
        if (ChronoUnit.MINUTES.between(lastCleanup, now) >= CLEANUP_INTERVAL_MINUTES) {
            requestCounters.entrySet().removeIf(entry -> entry.getValue().isExpired());
            lastCleanup = now;
            LOGGER.debug("Cleaned up expired rate limit entries");
        }
    }
    
    private static class RequestCounter {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final LocalDateTime startTime = LocalDateTime.now();
        private final LocalDateTime resetTime = startTime.plusMinutes(1);
        
        public boolean tryAcquire() {
            if (isExpired()) {
                return true; // Allow if window has expired
            }
            
            int current = requestCount.incrementAndGet();
            return current <= MAX_REQUESTS_PER_MINUTE;
        }
        
        public int getRequestCount() {
            return isExpired() ? 0 : requestCount.get();
        }
        
        public long getResetTime() {
            return ChronoUnit.SECONDS.between(LocalDateTime.now(), resetTime);
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(resetTime);
        }
    }
    
    public static class RateLimitInfo {
        private final int limit;
        private final int remaining;
        private final long resetTime;
        
        public RateLimitInfo(int limit, int remaining, long resetTime) {
            this.limit = limit;
            this.remaining = remaining;
            this.resetTime = resetTime;
        }
        
        public int getLimit() { return limit; }
        public int getRemaining() { return remaining; }
        public long getResetTime() { return resetTime; }
    }
} 