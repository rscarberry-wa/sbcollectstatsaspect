package com.rscarberry.sbcollectstatsaspect.micrometer;

import com.rscarberry.sbcollectstatsaspect.sumstats.SummaryStatsAccumulator;
import com.rscarberry.sbcollectstatsaspect.sumstats.SummaryStatsContainer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

@Component
public class CustomMetrics implements BiConsumer<String, SummaryStatsContainer> {

    private final MeterRegistry meterRegistry;

    private final Map<String, MetricsContainer> metricsMap = new ConcurrentHashMap<>();

    @Autowired
    public CustomMetrics(SummaryStatsAccumulator summaryStatsAccumulator, MeterRegistry meterRegistry) {
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
            minCountGauge = meterRegistry.gauge("log.stats", 
                List.of(Tag.of("endpoint", key), Tag.of("id", "min_count")), 
                new AtomicLong());
            maxCountGauge = meterRegistry.gauge("log.stats", 
                List.of(Tag.of("endpoint", key), Tag.of("id", "max_count")), 
                new AtomicLong());
            meanCountGauge = meterRegistry.gauge("log.stats", 
                List.of(Tag.of("endpoint", key), Tag.of("id", "mean_count")), 
                new AtomicLong());
            minLatencyGauge = meterRegistry.gauge("log.stats", 
                List.of(Tag.of("endpoint", key), Tag.of("id", "min_latency")), 
                new AtomicLong());
            maxLatencyGauge = meterRegistry.gauge("log.stats", 
                List.of(Tag.of("endpoint", key), Tag.of("id", "max_latency")), 
                new AtomicLong());
            meanLatencyGauge = meterRegistry.gauge("log.stats", 
                List.of(Tag.of("endpoint", key), Tag.of("id", "mean_latency")), 
                new AtomicLong());
            successGauge = meterRegistry.gauge("log.stats", 
                List.of(Tag.of("endpoint", key), Tag.of("id", "successes")), 
                new AtomicLong());
            failureGauge = meterRegistry.gauge("log.stats", 
                List.of(Tag.of("endpoint", key), Tag.of("id", "failures")), 
                new AtomicLong());
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

        private static String micrometerKey(String key) {
            return key.toLowerCase().replaceAll("_+", ".");
        }
    }
}
