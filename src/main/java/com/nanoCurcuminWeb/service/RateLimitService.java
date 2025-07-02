package com.nanoCurcuminWeb.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    
    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    
    // Rate limit: 5 attempts per hour per IP address
    private static final int MAX_ATTEMPTS = 5;
    private static final long TIME_WINDOW_SECONDS = 3600; // 1 hour
    
    public boolean tryConsume(String key) {
        Instant now = Instant.now();
        RateLimitInfo info = rateLimitMap.computeIfAbsent(key, k -> new RateLimitInfo());
        
        // Check if time window has passed
        if (now.isAfter(info.getWindowStart().plusSeconds(TIME_WINDOW_SECONDS))) {
            info.reset(now);
        }
        
        // Check if we can consume
        if (info.getAttempts() < MAX_ATTEMPTS) {
            info.incrementAttempts();
            return true;
        }
        
        return false;
    }
    
    public void resetBucket(String key) {
        rateLimitMap.remove(key);
    }
    
    private static class RateLimitInfo {
        private int attempts;
        private Instant windowStart;
        
        public RateLimitInfo() {
            this.attempts = 0;
            this.windowStart = Instant.now();
        }
        
        public void reset(Instant newStart) {
            this.attempts = 0;
            this.windowStart = newStart;
        }
        
        public void incrementAttempts() {
            this.attempts++;
        }
        
        public int getAttempts() {
            return attempts;
        }
        
        public Instant getWindowStart() {
            return windowStart;
        }
    }
} 