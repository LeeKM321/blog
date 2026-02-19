package com.codeit.blog.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class CachePerformanceAspect {

    private static final long SLOW_THRESHOLD_MS = 300;

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object measureCachePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // 실행 시간 로깅
            if (duration < 10) {
                // 10ms 미만은 캐시 히트로 간주
                log.debug("⚡ Cache HIT: {} - {}ms (args: {})", methodName, duration, args);
            } else if (duration < SLOW_THRESHOLD_MS) {
                // 100ms 미만은 정상
                log.debug("✅ Cache MISS: {} - {}ms (args: {})", methodName, duration, args);
            } else {
                // 100ms 이상은 느린 조회로 경고
                log.warn("🐌 Slow Query: {} - {}ms (args: {})", methodName, duration, args);
            }

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Cache Error: {} - {}ms (args: {})", methodName, duration, args, e);
            throw e;
        }
    }

}
