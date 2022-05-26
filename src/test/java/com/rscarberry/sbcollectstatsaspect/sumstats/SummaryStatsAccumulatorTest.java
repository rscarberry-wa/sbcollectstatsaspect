package com.rscarberry.sbcollectstatsaspect.sumstats;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SummaryStatsAccumulatorTest {

    @Test
    void logStats() {

        String key = "logStats test";
        SummaryStatsAccumulator accumulator = new SummaryStatsAccumulator();
        Random random = new Random(343984);

        for (int i=0; i<500; i++) {
            long count = 10 + random.nextInt(90);
            long latency = 5 + random.nextInt(45);
            boolean success = random.nextDouble() < 0.05;
            accumulator.addStats(key, success, count, latency);
        }

        accumulator.logStats();

    }
}