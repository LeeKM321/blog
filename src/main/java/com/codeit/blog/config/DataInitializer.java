package com.codeit.blog.config;

import com.codeit.blog.entity.Post;
import com.codeit.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 초기 데이터 생성
 *
 * ApplicationRunner를 구현하여 애플리케이션 시작 시 자동 실행
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final PostRepository postRepository;
    private final Random random = new Random();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (postRepository.count() > 0) {
            log.info("초기 데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("===== 초기 데이터 생성 시작 =====");

        // 1단계: 샘플 데이터 생성
        createSamplePosts();

        // 2단계: 대량 데이터 생성
        generateLargeDataset();

        // 통계 출력
        long totalCount = postRepository.count();
        log.info("===== 초기 데이터 생성 완료: 총 {}개 게시글 =====", totalCount);

        // 카테고리별 통계 출력
        log.info("카테고리별 게시글 수:");
        List<String> categories = List.of("Java", "Spring", "Database", "Architecture", "DevOps");
        categories.forEach(category -> {
            long count = postRepository.countByCategory(category);
            log.info("  - {}: {}개", category, count);
        });
    }

    /**
     * 샘플 게시글 생성
     */
    private void createSamplePosts() {
        log.info("📝 샘플 게시글 25개 생성 중...");

        List<Post> posts = new ArrayList<>();

        // Java 카테고리
        posts.add(createPost("Java 기초 문법 정리", "Java의 기본 문법을 정리한 글입니다.", "김자바", "Java", 150, 42));
        posts.add(createPost("Java Stream API 완벽 가이드", "Stream API의 모든 것을 알아봅니다.", "이스트림", "Java", 230, 67));
        posts.add(createPost("Java 멀티스레딩 이해하기", "Thread, Executor, CompletableFuture까지", "박스레드", "Java", 189, 54));
        posts.add(createPost("Java 메모리 관리와 GC", "Heap, Stack, GC 동작 원리", "최메모리", "Java", 276, 89));
        posts.add(createPost("Java 17의 새로운 기능", "Record, Sealed Class, Pattern Matching", "정자바", "Java", 198, 61));

        // Spring 카테고리
        posts.add(createPost("Spring Boot 시작하기", "Spring Boot의 기본 개념과 시작 방법", "김스프링", "Spring", 320, 95));
        posts.add(createPost("Spring Cache 완벽 정복", "캐시의 기본부터 고급 활용까지", "이캐시", "Spring", 412, 134));
        posts.add(createPost("Spring Security 인증과 인가", "보안 설정의 모든 것", "박시큐리티", "Spring", 289, 78));
        posts.add(createPost("Spring Data JPA 실전 활용", "JPA로 복잡한 쿼리 작성하기", "최제이피에이", "Spring", 356, 102));
        posts.add(createPost("Spring AOP 이해하기", "관점 지향 프로그래밍의 핵심", "정에이오피", "Spring", 201, 67));

        // Database 카테고리
        posts.add(createPost("MySQL 인덱스 최적화", "쿼리 성능을 높이는 인덱스 전략", "김디비", "Database", 267, 73));
        posts.add(createPost("Redis 실전 활용법", "분산 캐시와 세션 저장소", "이레디스", "Database", 389, 112));
        posts.add(createPost("PostgreSQL vs MySQL 비교", "어떤 데이터베이스를 선택할까?", "박포스트그레", "Database", 234, 65));
        posts.add(createPost("NoSQL 데이터 모델링", "MongoDB 스키마 설계 패턴", "최몽고", "Database", 198, 54));
        posts.add(createPost("트랜잭션 격리 수준 이해하기", "ACID와 격리 수준의 트레이드오프", "정트랜잭션", "Database", 312, 87));

        // Architecture 카테고리
        posts.add(createPost("마이크로서비스 아키텍처 패턴", "MSA 설계 시 고려사항", "김아키텍트", "Architecture", 445, 156));
        posts.add(createPost("이벤트 기반 아키텍처", "Event Sourcing과 CQRS", "이이벤트", "Architecture", 367, 98));
        posts.add(createPost("DDD 전술적 패턴", "Aggregate, Entity, Value Object", "박도메인", "Architecture", 298, 82));
        posts.add(createPost("클린 아키텍처 실천하기", "의존성 역전과 계층 분리", "최클린", "Architecture", 423, 134));
        posts.add(createPost("헥사고날 아키텍처", "포트와 어댑터 패턴", "정헥사", "Architecture", 289, 76));

        // DevOps 카테고리
        posts.add(createPost("Docker 컨테이너 기초", "이미지 빌드부터 배포까지", "김도커", "DevOps", 512, 178));
        posts.add(createPost("Kubernetes 완전 정복", "Pod, Service, Deployment 이해하기", "이쿠버", "DevOps", 678, 234));
        posts.add(createPost("CI/CD 파이프라인 구축", "Jenkins를 활용한 자동화", "박시아이", "DevOps", 445, 145));
        posts.add(createPost("모니터링 시스템 구축", "Prometheus와 Grafana", "최모니터", "DevOps", 389, 112));
        posts.add(createPost("AWS 인프라 설계", "VPC, ECS, RDS 구성", "정에이더블유에스", "DevOps", 523, 167));

        postRepository.saveAll(posts);
        log.info("✅ 샘플 게시글 생성 완료");
    }

    /**
     * 대량 데이터 생성
     */
    private void generateLargeDataset() {
        log.info("🚀 대량 데이터 생성 시작 (10,000개)...");

        String[] categories = {"Java", "Spring", "Database", "Architecture", "DevOps"};
        String[] authors = {"김개발", "이백엔드", "박프론트", "최데브옵스", "정풀스택"};
        String[][] topicsByCategory = {
                // Java
                {"Lambda", "Optional", "Generic", "Reflection", "Annotation", "NIO", "Collection", "Concurrency"},
                // Spring
                {"MVC", "WebFlux", "Batch", "Cloud", "Security", "Data", "Integration", "Boot"},
                // Database
                {"Index", "Transaction", "Replication", "Sharding", "Partitioning", "Query", "Backup", "Migration"},
                // Architecture
                {"Hexagonal", "Layered", "CQRS", "Event-Driven", "Microservices", "Monolith", "Serverless", "DDD"},
                // DevOps
                {"CI/CD", "Monitoring", "Logging", "Container", "Orchestration", "IaC", "Cloud", "Automation"}
        };

        List<Post> batch = new ArrayList<>();
        int batchSize = 500; // 500개씩 배치 처리

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            int categoryIndex = i % categories.length;
            String category = categories[categoryIndex];
            String author = authors[random.nextInt(authors.length)];
            String topic = topicsByCategory[categoryIndex][random.nextInt(topicsByCategory[categoryIndex].length)];

            String title = String.format("%s %s 가이드 #%d", category, topic, i + 26);
            String content = String.format(
                    "이것은 %s 카테고리의 %d번째 게시글입니다. " +
                            "%s에 대한 상세한 내용을 다룹니다. ",
                            category, i + 1, topic
            );

            int viewCount = random.nextInt(1000);
            int likeCount = random.nextInt(viewCount / 5 + 1);

            Post post = createPost(title, content, author, category, viewCount, likeCount);
            batch.add(post);

            // 배치 크기만큼 모이면 저장
            if (batch.size() >= batchSize) {
                postRepository.saveAll(batch);
                batch.clear();

                if ((i + 1) % 2000 == 0) {
                    log.info("  진행률: {}/10,000 ({}%)", i + 1, (i + 1) / 100);
                }
            }
        }

        // 남은 데이터 저장
        if (!batch.isEmpty()) {
            postRepository.saveAll(batch);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("✅ 대량 데이터 생성 완료 (소요 시간: {}초)", elapsed / 1000.0);
    }

    private Post createPost(String title, String content, String author, String category, int viewCount, int likeCount) {
        Post post = new Post(title, content, author, category);

        // 조회수와 좋아요 수 설정
        for (int i = 0; i < viewCount; i++) {
            post.incrementViewCount();
        }
        for (int i = 0; i < likeCount; i++) {
            post.incrementLikeCount();
        }

        return post;
    }
}
