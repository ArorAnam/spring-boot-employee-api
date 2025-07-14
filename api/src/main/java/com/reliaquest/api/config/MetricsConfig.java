package com.reliaquest.api.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public TaggedCircuitBreakerMetrics circuitBreakerMetrics(
            CircuitBreakerRegistry circuitBreakerRegistry, MeterRegistry meterRegistry) {
        TaggedCircuitBreakerMetrics metrics =
                TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    @Bean
    public TaggedRateLimiterMetrics rateLimiterMetrics(
            RateLimiterRegistry rateLimiterRegistry, MeterRegistry meterRegistry) {
        TaggedRateLimiterMetrics metrics = TaggedRateLimiterMetrics.ofRateLimiterRegistry(rateLimiterRegistry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    @Bean
    public CaffeineCacheMetrics employeesCacheMetrics(CacheManager cacheManager, MeterRegistry meterRegistry) {
        var cache = cacheManager.getCache("employees");
        if (cache instanceof CaffeineCache) {
            CaffeineCacheMetrics metrics =
                    new CaffeineCacheMetrics(((CaffeineCache) cache).getNativeCache(), "employees", Tags.empty());
            metrics.bindTo(meterRegistry);
            return metrics;
        }
        return null;
    }

    @Bean
    public CaffeineCacheMetrics employeeByIdCacheMetrics(CacheManager cacheManager, MeterRegistry meterRegistry) {
        var cache = cacheManager.getCache("employee-by-id");
        if (cache instanceof CaffeineCache) {
            CaffeineCacheMetrics metrics =
                    new CaffeineCacheMetrics(((CaffeineCache) cache).getNativeCache(), "employee-by-id", Tags.empty());
            metrics.bindTo(meterRegistry);
            return metrics;
        }
        return null;
    }
}
