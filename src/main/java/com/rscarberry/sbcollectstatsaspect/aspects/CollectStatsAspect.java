package com.rscarberry.sbcollectstatsaspect.aspects;

import com.rscarberry.sbcollectstatsaspect.sumstats.SummaryStatsAccumulator;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Service
@Slf4j
public class CollectStatsAspect {

    @Autowired
    private SummaryStatsAccumulator statsAccumulator;

    @Around("@annotation(com.rscarberry.sbcollectstatsaspect.aspects.CollectStats)")
    public Object collectCountAndLatencyStats(ProceedingJoinPoint joinPoint) throws Throwable {
        var startMs = System.currentTimeMillis();
        AtomicLong count = new AtomicLong();
        AtomicBoolean success = new AtomicBoolean();
        // The class/type of "proceed" is simply the return type of the method that's annotated with @CollectStats
        var proceed = joinPoint.proceed();
        if (proceed instanceof Mono) {
            proceed = ((Mono<?>) proceed).doOnNext(v -> {
                if (v instanceof Collection) {
                    count.set(((Collection) v).size());
                }
            })
            .doFinally(st -> {
                statsAccumulator.addStats(
                    joinPoint.toShortString(), 
                    st == SignalType.ON_COMPLETE, 
                    count.get(), 
                    System.currentTimeMillis() - startMs);
            });
        } else {
            log.info("......... proceed is a {}", proceed.getClass().getName());
        }
        return proceed;
    }
}
