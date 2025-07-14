package com.reliaquest.api.health;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Health indicator for the Mock Employee API service.
 */
@Component
@Slf4j
public class MockEmployeeApiHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    private final RateLimiter rateLimiter;
    private final CacheManager cacheManager;

    @Value("${employee.api.base-url:http://localhost:8112/api/v1/employee}")
    private String baseUrl;

    public MockEmployeeApiHealthIndicator(
            RestTemplate restTemplate,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RateLimiterRegistry rateLimiterRegistry,
            CacheManager cacheManager) {
        this.restTemplate = restTemplate;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("employee-service");
        this.rateLimiter = rateLimiterRegistry.rateLimiter("employee-service");
        this.cacheManager = cacheManager;
    }

    @Override
    public Health health() {
        try {
            // Perform a simple GET request to check if the service is available
            restTemplate.getForEntity(baseUrl, String.class);

            // Get circuit breaker metrics
            CircuitBreaker.Metrics cbMetrics = circuitBreaker.getMetrics();

            // Get cache statistics
            long cacheHits = 0;
            long cacheMisses = 0;
            if (cacheManager.getCache("employees") instanceof CaffeineCache) {
                Cache<Object, Object> nativeCache =
                        ((CaffeineCache) cacheManager.getCache("employees")).getNativeCache();
                cacheHits = nativeCache.stats().hitCount();
                cacheMisses = nativeCache.stats().missCount();
            }

            return Health.up()
                    .withDetail("service", "Mock Employee API")
                    .withDetail("url", baseUrl)
                    .withDetail("status", "UP")
                    .withDetail("description", "Mock Employee API is responding")
                    .withDetail("circuitBreaker.state", circuitBreaker.getState())
                    .withDetail("circuitBreaker.failureRate", cbMetrics.getFailureRate())
                    .withDetail("circuitBreaker.slowCallRate", cbMetrics.getSlowCallRate())
                    .withDetail(
                            "rateLimiter.availablePermissions",
                            rateLimiter.getMetrics().getAvailablePermissions())
                    .withDetail("cache.hitRate", cacheHits > 0 ? (double) cacheHits / (cacheHits + cacheMisses) : 0)
                    .build();

        } catch (Exception e) {
            log.warn("Mock Employee API health check failed", e);

            return Health.down()
                    .withDetail("service", "Mock Employee API")
                    .withDetail("url", baseUrl)
                    .withDetail("status", "DOWN")
                    .withDetail("error", e.getMessage())
                    .withDetail("description", "Mock Employee API is not responding")
                    .withDetail("circuitBreaker.state", circuitBreaker.getState())
                    .build();
        }
    }
}
