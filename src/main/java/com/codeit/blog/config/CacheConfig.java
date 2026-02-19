package com.codeit.blog.config;

import com.codeit.blog.entity.Post;
import com.codeit.blog.repository.PostRepository;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Bean
    public CacheManager caffeineCacheManager() {
        log.info("caffeine 캐시 매니저 초기화");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 카페인 기본 설정
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000) // 캐시 최대 1000개. 넘으면 LRU(Least Recently Used) 방식으로 제거
                .expireAfterWrite(Duration.ofMinutes(10)) // 저장 후 10분이 지나면 자동으로 만료
                .recordStats() // 통계 수집 활성화
        );

        // 캐시별 개별 설정
        cacheManager.registerCustomCache("posts",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(Duration.ofSeconds(10))
                        .recordStats()
                        .build()
        );

        cacheManager.registerCustomCache("postsByCategory",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterWrite(Duration.ofMinutes((long) (3 + (Math.random()*3))))
                        .recordStats()
                        .build()
        );

        cacheManager.registerCustomCache("popularPosts",
                Caffeine.newBuilder()
                        .maximumSize(10)
                        .expireAfterWrite(Duration.ofMinutes(1))
                        .recordStats()
                        .build()
        );

        return cacheManager;
    }

    @Bean
    public LoadingCache<Long, Post> postLoadingCache(PostRepository postRepository) {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .refreshAfterWrite(Duration.ofMinutes(2)) // 캐시 미리 갱신하는 로직 (LoadingCache와 함께 작성)
                .recordStats()
                // build() 안에 로딩 로직을 정의.
                // 캐시 미스 시 자동으로 이 로직이 실행되고, 동시 요청은 첫번째만 실행되고 나머지는 기다립니다.
                .build(key -> {
                    log.info("LoadingCache: DB에서 로드 중... key={}", key);
                    return postRepository.findById(key)
                            .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + key));
                });
    }


}












