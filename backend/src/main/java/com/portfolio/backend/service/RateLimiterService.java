package com.portfolio.backend.service;

import com.portfolio.backend.config.RateLimiterConfig;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;

import java.util.concurrent.*;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimiterService {

    private final RateLimiterConfig config;

    // Per-IP rate limit state
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> lastResetTimes = new ConcurrentHashMap<>();

    public RateLimiterService(RateLimiterConfig config) {
        this.config = config;
    }

    /**
     * Check if the given IP has exceeded the rate limit.
     * Called from the filter with the client IP already resolved.
     *
     * @return RateLimitResult containing whether the request is allowed and a message if not
     */
    public RateLimitResult checkRateLimit(String ip) {
        Instant now = Instant.now();

        // Get or create the counter for this IP
        AtomicInteger count = requestCounts.computeIfAbsent(ip, k -> new AtomicInteger(0));
        Instant lastReset = lastResetTimes.get(ip);

        // Window reset logic
        if (lastReset == null || Duration.between(lastReset, now).getSeconds() >= config.getWindowSeconds()) {
            // Reset the window
            count.set(1);
            lastResetTimes.put(ip, now);
            return new RateLimitResult(true, null);
        }

        // Increment counter
        int currentCount = count.incrementAndGet();

        if (currentCount > config.getMaxRequests()) {
            return new RateLimitResult(false, "Too many requests. Please try again later.");
        }

        return new RateLimitResult(true, null);
    }

    /**
     * Reset the rate limit window for a given IP (used when IP changes or on logout).
     */
    public void resetRateLimit(String ip) {
        requestCounts.remove(ip);
        lastResetTimes.remove(ip);
    }

    /** Simple holder for rate limit check results. */
    public static class RateLimitResult {
        private final boolean allowed;
        private final String message;

        public RateLimitResult(boolean allowed, String message) {
            this.allowed = allowed;
            this.message = message;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getMessage() {
            return message;
        }
    }
}