package com.codeit.blog.monitoring;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheHealthCheckIndicator implements HealthIndicator {

    private final CacheManager cacheManager;

    @Override
    public Health health() {
        try {
            Map<String, Object> details = new HashMap<>();
            boolean hasLowHitRate = false;

            for (String cacheName : cacheManager.getCacheNames()) {
                Cache cache = cacheManager.getCache(cacheName);

                if (cache instanceof CaffeineCache caffeineCache) {
                    com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                            caffeineCache.getNativeCache();

                    CacheStats stats = nativeCache.stats();
                    double hitRate = stats.hitRate();
                    long size = nativeCache.estimatedSize();

                    Map<String, Object> cacheDetails = Map.of(
                            "hitRate", String.format("%.2f%%", hitRate * 100),
                            "hitCount", stats.hitCount(),
                            "missCount", stats.missCount(),
                            "size", size,
                            "evictionCount", stats.evictionCount()
                    );

                    details.put(cacheName, cacheDetails);

                    // 적중률이 50% 미만이고 요청이 100회 이상이면 경고
                    if (hitRate < 0.5 && stats.requestCount() > 100) {
                        hasLowHitRate = true;
                    }
                }
            }

            if (hasLowHitRate) {
                return Health.down()
                        .withDetails(details)
                        .withDetail("warning", "일부 캐시의 적중률이 낮습니다")
                        .build();
            }

            return Health.up().withDetails(details).build();

        } catch (Exception e) {
            log.error("캐시 Health 체크 중 오류 발생", e);
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
}














