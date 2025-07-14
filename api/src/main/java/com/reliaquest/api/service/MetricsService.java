package com.reliaquest.api.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for tracking custom business metrics.
 */
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public void recordApiCall(String endpoint, boolean success, long durationMs) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("api.request.duration")
                .description("Duration of API requests")
                .tag("endpoint", endpoint)
                .tag("success", String.valueOf(success))
                .register(meterRegistry));

        Counter.builder("api.request.count")
                .description("Count of API requests")
                .tag("endpoint", endpoint)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .increment();
    }

    public void recordCircuitBreakerEvent(String event) {
        Counter.builder("circuit.breaker.events")
                .description("Circuit breaker events")
                .tag("event", event)
                .register(meterRegistry)
                .increment();
    }

    public void recordCacheEvent(String cache, String event) {
        Counter.builder("cache.events")
                .description("Cache events")
                .tag("cache", cache)
                .tag("event", event)
                .register(meterRegistry)
                .increment();
    }

    public void recordRateLimitEvent(boolean rejected) {
        Counter.builder("rate.limit.events")
                .description("Rate limit events")
                .tag("rejected", String.valueOf(rejected))
                .register(meterRegistry)
                .increment();
    }

    public void recordEmployeeCount(int count) {
        Gauge.builder("employees.total.count", () -> count)
                .description("Total number of employees")
                .register(meterRegistry);
    }

    public void recordAverageSalary(double avgSalary) {
        Gauge.builder("employees.average.salary", () -> avgSalary)
                .description("Average employee salary")
                .register(meterRegistry);
    }
}
