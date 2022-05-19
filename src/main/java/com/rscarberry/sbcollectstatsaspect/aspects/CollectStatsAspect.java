package com.rscarberry.sbcollectstatsaspect.aspects;

import com.rscarberry.sbcollectstatsaspect.sumstats.SummaryStatsAccumulator;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Aspect
@Service
@Slf4j
public class CollectStatsAspect {

    @Autowired
    private SummaryStatsAccumulator statsAccumulator;

    @Around("@annotation(com.rscarberry.sbcollectstatsaspect.aspects.CollectStats)")
    public Object collectCountAndLatencyStats(ProceedingJoinPoint joinPoint) throws Throwable {
        var startMs = System.currentTimeMillis();
        long count = 0;
        long latency = 0;
        boolean success = false;
        try {
            // The class/type of "proceed" is simply the return type of the method that's annotated with @CollectStats
            var proceed = joinPoint.proceed();
            if (proceed instanceof Collection) {
                count = ((Collection) proceed).size();
            }
            latency = System.currentTimeMillis() - startMs;
            success = true;
            return proceed;
        } finally {
            statsAccumulator.addStats(joinPoint.toShortString(), success, count, latency);
        }
    }

}
