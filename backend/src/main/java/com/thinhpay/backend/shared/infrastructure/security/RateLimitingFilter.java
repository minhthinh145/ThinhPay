package com.thinhpay.backend.shared.infrastructure.security;

import com.thinhpay.backend.shared.infrastructure.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Filter using Bucket4j.
 *
 * Implements token bucket algorithm to limit request rates per IP/endpoint.
 * Uses in-memory buckets for fast performance.
 * TODO: Add Redis integration for distributed rate limiting across multiple instances.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;

    /**
     * In-memory cache of buckets for performance.
     * Key format: "ip:endpoint"
     */
    private final ConcurrentHashMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();

        // DEBUG: Log every request
        log.info("🔍 RateLimitingFilter: Processing request from IP {} to {}", clientIp, endpoint);

        // Get or create bucket for this client+endpoint combination
        Bucket bucket = resolveBucket(clientIp, endpoint);

        // Try to consume 1 token
        boolean allowed = bucket.tryConsume(1);

        // Get available tokens for debugging
        long availableTokens = bucket.getAvailableTokens();

        log.info("📊 Rate limit check: {} for {} on {} (available tokens: {})",
            allowed ? "ALLOWED" : "DENIED", clientIp, endpoint, availableTokens);

        if (allowed) {
            // Token available - allow request
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded - return 429
            long waitForRefill = bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill() / 1_000_000_000;

            response.setStatus(429); // Too Many Requests
            response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"success\":false,\"status\":429,\"message\":\"Rate limit exceeded\"" +
                ",\"retryAfter\":\"%d seconds\",\"endpoint\":\"%s\"}",
                waitForRefill, endpoint
            ));

            log.warn("Rate limit exceeded for IP: {} on endpoint: {} - retry after {}s",
                clientIp, endpoint, waitForRefill);
        }
    }

    /**
     * Get or create a bucket for the given client IP and endpoint.
     */
    private Bucket resolveBucket(String clientIp, String endpoint) {
        String key = clientIp + ":" + endpoint;

        boolean isNewBucket = !bucketCache.containsKey(key);

        Bucket bucket = bucketCache.computeIfAbsent(key, k -> {
            RateLimitConfig.EndpointLimit limit = rateLimitConfig.getLimitForEndpoint(endpoint);
            log.info("🆕 Creating NEW bucket for key: {} with limit: {} requests per {}",
                key, limit.getCapacity(), limit.getInterval());
            return createBucket(limit);
        });

        if (!isNewBucket) {
            log.debug("♻️  Reusing EXISTING bucket for key: {}", key);
        }

        return bucket;
    }

    /**
     * Create a new bucket with the specified limit configuration.
     */
    private Bucket createBucket(RateLimitConfig.EndpointLimit limit) {
        long intervalMillis = limit.getIntervalMillis();

        Bandwidth bandwidth = Bandwidth.classic(
            limit.getCapacity(),
            Refill.intervally(limit.getTokens(), Duration.ofMillis(intervalMillis))
        );

        return Bucket.builder()
            .addLimit(bandwidth)
            .build();
    }

    /**
     * Extract client IP address from request.
     * Checks X-Forwarded-For header first (for proxies/load balancers).
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    /**
     * Skip rate limiting for public static resources and Swagger.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();

        log.debug("🔍 shouldNotFilter check for path: {}", path);

        // Skip Swagger/OpenAPI endpoints
        if (path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-resources") ||
            path.startsWith("/webjars")) {
            log.debug("  → Skipping (Swagger/OpenAPI)");
            return true;
        }

        // Skip actuator health check
        if (path.startsWith("/actuator/health")) {
            log.debug("  → Skipping (Actuator)");
            return true;
        }

        log.debug("  → NOT skipping, rate limiting will apply");
        return false;
    }
}
