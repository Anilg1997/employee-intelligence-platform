package com.employeeintelligence.observability;

import io.micrometer.core.instrument.MeterRegistry;

/** Small guard for optional instrumentation in unit-tested service instances. */
public final class MetricsSupport {
    private MetricsSupport() { }

    public static void increment(MeterRegistry registry, String name, String... tags) {
        if (registry != null) {
            registry.counter(name, tags).increment();
        }
    }
}
