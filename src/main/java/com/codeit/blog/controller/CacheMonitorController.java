package com.codeit.blog.controller;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheMonitorController {

    private final CacheManager cacheManager;

    /**
     * 전체 캐시 목록 및 간단한 통계
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCaches() {
        Map<String, Object> result = new HashMap<>();

        Map<String, Map<String, Object>> caches = cacheManager.getCacheNames().stream()
                .collect(Collectors.toMap(
                        name -> name,
                        name -> {
                            Cache cache = cacheManager.getCache(name);
                            if (cache instanceof CaffeineCache caffeineCache) {
                                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                                        caffeineCache.getNativeCache();

                                CacheStats stats = nativeCache.stats();
                                double hitRate = stats.requestCount() > 0
                                        ? stats.hitRate() * 100
                                        : 0.0;

                                return Map.of(
                                        "size", nativeCache.estimatedSize(),
                                        "hitRate", String.format("%.2f%%", hitRate),
                                        "requestCount", stats.requestCount()
                                );
                            }
                            return Map.of("type", "unknown");
                        }
                ));

        result.put("caches", caches);
        result.put("cacheCount", caches.size());

        return ResponseEntity.ok(result);
    }

    /**
     * 특정 캐시의 상세 통계
     */
    @GetMapping("/{cacheName}/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);

        if (cache == null) {
            return ResponseEntity.notFound().build();
        }

        if (!(cache instanceof CaffeineCache caffeineCache)) {
            return ResponseEntity.ok(Map.of(
                    "cacheName", cacheName,
                    "type", cache.getClass().getSimpleName(),
                    "message", "통계를 지원하지 않는 캐시 타입입니다"
            ));
        }

        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                caffeineCache.getNativeCache();

        CacheStats stats = nativeCache.stats();
        long requestCount = stats.requestCount();
        double hitRate = requestCount > 0 ? stats.hitRate() : 0.0;
        double missRate = requestCount > 0 ? stats.missRate() : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("cacheName", cacheName);
        result.put("size", nativeCache.estimatedSize());
        result.put("hitCount", stats.hitCount());
        result.put("missCount", stats.missCount());
        result.put("requestCount", requestCount);
        result.put("hitRate", String.format("%.2f%%", hitRate * 100));
        result.put("missRate", String.format("%.2f%%", missRate * 100));
        result.put("loadSuccessCount", stats.loadSuccessCount());
        result.put("loadFailureCount", stats.loadFailureCount());
        result.put("evictionCount", stats.evictionCount());
        result.put("evictionWeight", stats.evictionWeight());

        // 캐시 효율성 평가
        result.put("evaluation", evaluateCacheEfficiency(hitRate));

        return ResponseEntity.ok(result);
    }

    /**
     * 특정 캐시 비우기
     */
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);

        if (cache == null) {
            return ResponseEntity.notFound().build();
        }

        cache.clear();
        log.info("캐시 '{}' 초기화 완료", cacheName);

        return ResponseEntity.ok(Map.of(
                "message", cacheName + " 캐시가 초기화되었습니다"
        ));
    }

    /**
     * 모든 캐시 비우기
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        int count = 0;
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                count++;
            }
        }

        log.info("전체 캐시 초기화 완료: {}개", count);

        return ResponseEntity.ok(Map.of(
                "message", "모든 캐시가 초기화되었습니다",
                "count", count
        ));
    }

    /**
     * 캐시 효율성 평가
     */
    private Map<String, String> evaluateCacheEfficiency(double hitRate) {
        String level;
        String message;
        String recommendation;

        if (Double.isNaN(hitRate) || hitRate == 0.0) {
            level = "N/A";
            message = "캐시 사용 기록이 없습니다";
            recommendation = "캐시가 활용되고 있는지 확인해보세요";
        } else if (hitRate >= 0.9) {
            level = "EXCELLENT";
            message = "매우 효과적인 캐시 사용";
            recommendation = "현재 설정을 유지하세요";
        } else if (hitRate >= 0.7) {
            level = "GOOD";
            message = "양호한 캐시 사용률";
            recommendation = "TTL이나 캐시 크기 조정을 고려해보세요";
        } else if (hitRate >= 0.5) {
            level = "FAIR";
            message = "보통 수준의 캐시 사용률";
            recommendation = "캐시 전략(TTL, 크기, 키 설계)을 재검토하세요";
        } else {
            level = "POOR";
            message = "캐시 효율이 낮습니다";
            recommendation = "캐시 키, TTL, 접근 패턴을 전면 재검토하세요";
        }

        return Map.of(
                "level", level,
                "message", message,
                "recommendation", recommendation
        );
    }



}





















