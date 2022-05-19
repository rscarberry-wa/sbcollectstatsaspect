package com.rscarberry.sbcollectstatsaspect.sumstats;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.time.Instant;

public class SummaryStatsContainer {

    private final Instant creationInstant = Instant.now();
    private final SummaryStatistics countStatistics = new SummaryStatistics();
    private final SummaryStatistics latencyStatistics = new SummaryStatistics();
    private long successCount;
    private long failureCount;

    public Instant getCreationInstant() {
        return creationInstant;
    }

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
