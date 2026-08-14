package com.portfolio.backend.filter;

import com.portfolio.backend.config.RateLimiterConfig;
import com.portfolio.backend.service.RateLimiterService;
import com.portfolio.backend.service.RateLimiterService.RateLimitResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final RateLimiterConfig rateLimiterConfig;

    public RateLimitingFilter(RateLimiterService rateLimiterService, RateLimiterConfig rateLimiterConfig) {
        this.rateLimiterService = rateLimiterService;
        this.rateLimiterConfig = rateLimiterConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();

        // Only rate-limit POST /api/contact, not GET /api/health
        if ("POST".equals(method) && path.startsWith("/api/contact")) {
            String ip = getClientIp(request);
            RateLimitResult result = rateLimiterService.checkRateLimit(ip);

            if (!result.isAllowed()) {
                // Return HTTP 429 with JSON response
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                // Retry-After header: suggest waiting until the window resets
                long resetSeconds = rateLimiterConfig.getWindowSeconds();
                response.setHeader("Retry-After", Long.toString(resetSeconds));

                String json = String.format(
                        "{\"status\":429,\"message\":\"%s\"}",
                        result.getMessage()
                );
                response.getWriter().write(json);
                response.getWriter().flush();
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Obtain the client IP, handling X-Forwarded-For for reverse proxies
     * (e.g., Render). Only trusts X-Forwarded-For if it contains a plausible
     * non-loopback IP; otherwise falls back to the direct remote address.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            String firstIp = forwardedFor.split(",")[0].trim();
            // Simple validation: if the first IP is not loopback, use it
            if (!firstIp.equals("127.0.0.1") && firstIp.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
                ip = firstIp;
            }
        }

        // Fallback: X-Real-IP header
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && realIp.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
            ip = realIp;
        }

        return ip;
    }
}