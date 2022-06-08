package com.rscarberry.sbcollectstatsaspect.sumstats;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Component
@Slf4j
public class SummaryStatsAccumulator {

    private Map<String, SummaryStatsContainer> statsMap = new ConcurrentHashMap<>();
    private List<BiConsumer<String, SummaryStatsContainer>> summaryStatsConsumers = new ArrayList<>();

    private Timer timer;

    @Autowired
    public SummaryStatsAccumulator(MeterRegistry meterRegistry) {
        this.timer = meterRegistry.timer("logstats.execution", "id", "method_invocation");
    }

    public void addSummaryStatsConsumer(BiConsumer<String, SummaryStatsContainer> consumer) {
        summaryStatsConsumers.add(consumer);
    }

    public void addStats(String key, boolean success, long count, long latency) {
        statsMap.compute(key, (k, statsContainer) -> {
            if (statsContainer == null) {
                statsContainer = new SummaryStatsContainer();
            }
            log.info("..... addStats {}: success = {}, count = {}, latency = {}", key, success, count, latency);
            statsContainer.registerSuccessOrFailure(success);
            if (success) {
                statsContainer.addCount(count);
                statsContainer.addLatency(latency);
            }
            return statsContainer;
        });
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void logStats() {
        this.timer.record(() -> {
        // Create a copy of the stats map sorted by key
        Map<String, SummaryStatsContainer> statsMapCopy = new TreeMap<>();
        // Copy contents of stats map to the copy in a threadsafe way.
        statsMap.replaceAll((key, statsContainer) -> {
            statsMapCopy.put(key, statsContainer);
            return new SummaryStatsContainer();
        });
        var consumers = summaryStatsConsumers.toArray(new BiConsumer[0]);
        // Now, do the actual work.
        statsMapCopy.entrySet()
                .forEach(entry -> {
                    String key = entry.getKey();
                    SummaryStatsContainer statsContainer = entry.getValue();
                    StatisticalSummary countSummary = statsContainer.getCountStatistics().getSummary();
                    StatisticalSummary latencySummary = statsContainer.getLatencyStatistics().getSummary();
                    long successCount = statsContainer.getSuccessCount();
                    long failureCount = statsContainer.getFailureCount();
                    log.info("Statistics for {}:\n\tNumber of returned values: {}\n\tLatency: {}\n\t{} successes, {} failures",
                            key,
                            statsString(countSummary),
                            statsString(latencySummary),
                            successCount,
                            failureCount);
                    // Forward to consumers, if any.
                    for (var consumer: consumers) {
                        ((BiConsumer<String, SummaryStatsContainer>) consumer).accept(key, statsContainer);
                    }
                });
            });
    }

    public static String statsString(StatisticalSummary statisticalSummary) {
        return String.format("minimum = %d, maximum = %d, mean = %.2f",
                ((long) statisticalSummary.getMin()),
                ((long) statisticalSummary.getMax()),
                statisticalSummary.getMean());
    }
}
