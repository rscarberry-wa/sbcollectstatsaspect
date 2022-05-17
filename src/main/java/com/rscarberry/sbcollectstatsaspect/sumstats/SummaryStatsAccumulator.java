package com.rscarberry.sbcollectstatsaspect.sumstats;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SummaryStatsAccumulator {

    private Map<String, StatsContainer> statsMap = new ConcurrentHashMap<>();

    public void addStats(String key, boolean success, long count, long latency) {
        statsMap.compute(key, (k, statsContainer) -> {
            if (statsContainer == null) {
                statsContainer = new StatsContainer();
            }
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
        // Create a copy of the stats map sorted by key
        Map<String, StatsContainer> statsMapCopy = new TreeMap<>();
        // Copy contents of stats map to the copy in a threadsafe way.
        statsMap.replaceAll((key, statsContainer) -> {
            statsMapCopy.put(key, statsContainer);
            return new StatsContainer();
        });
        // Now, do the actual work.
        statsMapCopy.entrySet()
                .forEach(entry -> {
                    String key = entry.getKey();
                    StatsContainer statsContainer = entry.getValue();
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
                });
    }

    private static String statsString(StatisticalSummary statisticalSummary) {
        return String.format("minimum = %d, maximum = %d, mean = %.2f",
                ((long) statisticalSummary.getMin()),
                ((long) statisticalSummary.getMax()),
                statisticalSummary.getMean());
    }

    private static class StatsContainer {

        private SummaryStatistics countStatistics = new SummaryStatistics();
        private SummaryStatistics latencyStatistics = new SummaryStatistics();
        private long successCount;
        private long failureCount;

        public SummaryStatistics getCountStatistics() {
            return countStatistics;
        }

        public SummaryStatistics getLatencyStatistics() {
            return latencyStatistics;
        }

        public long getSuccessCount() {
            return successCount;
        }

        public long getFailureCount() {
            return failureCount;
        }

        public void addCount(long count) {
            countStatistics.addValue(count);
        }

        public void addLatency(long latency) {
            latencyStatistics.addValue(latency);
        }

        public void registerSuccessOrFailure(boolean success) {
            if (success) {
                successCount++;
            } else {
                failureCount++;
            }
        }
    }
}
