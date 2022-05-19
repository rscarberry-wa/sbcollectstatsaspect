package com.rscarberry.sbcollectstatsaspect.health;

import com.rscarberry.sbcollectstatsaspect.sumstats.SummaryStatsAccumulator;
import com.rscarberry.sbcollectstatsaspect.sumstats.SummaryStatsContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

@Component
public class CollectStatsHealthIndicator extends AbstractHealthIndicator implements BiConsumer<String, SummaryStatsContainer> {

    private final SummaryStatsAccumulator summaryStatsAccumulator;
    private final Map<String, String> healthMap = new ConcurrentHashMap<>();

    @Autowired // Constructor autowiring leads to more immutability (not quite immutable, since healthMap changes)
    public CollectStatsHealthIndicator(SummaryStatsAccumulator summaryStatsAccumulator) {
        this.summaryStatsAccumulator = summaryStatsAccumulator;
        summaryStatsAccumulator.addSummaryStatsConsumer(this);
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        AtomicReference<Health.Builder> builderRef = new AtomicReference<>(builder.up());
        healthMap.forEach((detail, value) -> builderRef.set(builderRef.get().withDetail(detail, value)));
    }

    @Override
    public void accept(String s, SummaryStatsContainer summaryStatsContainer) {
        // The number of seconds from the instantiation of summaryStatsContainer to the call to this method.
        long seconds = Duration.between(summaryStatsContainer.getCreationInstant(), Instant.now()).toSeconds();
        healthMap.put(String.format("%s -- counts: ", s),
                String.format(" for previous %d seconds: %s", seconds,
                    SummaryStatsAccumulator.statsString(summaryStatsContainer.getCountStatistics())));
        healthMap.put(String.format("%s -- latency: ", s),
                String.format("for previous %d seconds: %s", seconds,
                    SummaryStatsAccumulator.statsString(summaryStatsContainer.getLatencyStatistics())));
        healthMap.put(String.format("%s -- successes and failures: ", s),
                String.format("in previous %d seconds: %d, %d", seconds,
                        summaryStatsContainer.getSuccessCount(),
                        summaryStatsContainer.getSuccessCount()));
    }
}
