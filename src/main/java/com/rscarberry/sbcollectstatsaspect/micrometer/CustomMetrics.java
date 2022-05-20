package com.rscarberry.sbcollectstatsaspect.micrometer;

import com.rscarberry.sbcollectstatsaspect.sumstats.SummaryStatsAccumulator;
import com.rscarberry.sbcollectstatsaspect.sumstats.SummaryStatsContainer;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

@Component
public class CustomMetrics implements BiConsumer<String, SummaryStatsContainer> {

    private final SummaryStatsAccumulator summaryStatsAccumulator;
    private final MeterRegistry meterRegistry;

    private final Map<String, MetricsContainer> metricsMap = new ConcurrentHashMap<>();

    @Autowired
    public CustomMetrics(SummaryStatsAccumulator summaryStatsAccumulator, MeterRegistry meterRegistry) {
        this.summaryStatsAccumulator = summaryStatsAccumulator;
        this.meterRegistry = meterRegistry;
        summaryStatsAccumulator.addSummaryStatsConsumer(this);
    }

    @Override
    public void accept(String s, SummaryStatsContainer summaryStatsContainer) {
        metricsMap.computeIfAbsent(s, key -> new MetricsContainer(key, meterRegistry))
                .update(summaryStatsContainer);
    }

    private static class MetricsContainer {

        private final AtomicLong minCountGauge;
        private final AtomicLong maxCountGauge;
        private final AtomicLong meanCountGauge;

        private final AtomicLong minLatencyGauge;
        private final AtomicLong maxLatencyGauge;
        private final AtomicLong meanLatencyGauge;

        private final AtomicLong successGauge;
        private final AtomicLong failureGauge;

        private MetricsContainer(String key, MeterRegistry meterRegistry) {
            minCountGauge = meterRegistry.gauge(String.format("%s_min_count", key), new AtomicLong());
            maxCountGauge = meterRegistry.gauge(String.format("%s_max_count", key), new AtomicLong());
            meanCountGauge = meterRegistry.gauge(String.format("%s_mean_count", key), new AtomicLong());
            minLatencyGauge = meterRegistry.gauge(String.format("%s_min_latency", key), new AtomicLong());
            maxLatencyGauge = meterRegistry.gauge(String.format("%s_max_latency", key), new AtomicLong());
            meanLatencyGauge = meterRegistry.gauge(String.format("%s_mean_latency", key), new AtomicLong());
            successGauge = meterRegistry.gauge(String.format("%s_successes", key), new AtomicLong());
            failureGauge = meterRegistry.gauge(String.format("%s_failures", key), new AtomicLong());
        }

        public void update(SummaryStatsContainer summaryStatsContainer) {
            SummaryStatistics countStats = summaryStatsContainer.getCountStatistics();
            minCountGauge.set((long) countStats.getMin());
            maxCountGauge.set((long) countStats.getMax());
            meanCountGauge.set(Math.round(countStats.getMean()));
            SummaryStatistics latencyStats = summaryStatsContainer.getLatencyStatistics();
            minLatencyGauge.set((long) latencyStats.getMin());
            maxLatencyGauge.set((long) latencyStats.getMax());
            meanLatencyGauge.set(Math.round(latencyStats.getMean()));
            successGauge.set(summaryStatsContainer.getSuccessCount());
            failureGauge.set(summaryStatsContainer.getFailureCount());
        }
    }
}
