package com.codeit.blog.controller;

import com.codeit.blog.dto.PostRequest;
import com.codeit.blog.dto.PostResponse;
import com.codeit.blog.entity.Post;
import com.codeit.blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    /**
     * 전체 게시글 조회
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<Post> posts = postService.findAll();
        List<PostResponse> responses = posts.stream()
                .map(PostResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * 게시글 단건 조회
     *
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPost(@PathVariable Long id) {
        long start = System.currentTimeMillis();

        Post post = postService.findById(id);

        long end = System.currentTimeMillis();
        long duration = end-start;

        boolean fromCache = duration < 50;

        return ResponseEntity.ok(Map.of(
                "post", PostResponse.from(post, fromCache),
                "responseTime", duration + "ms",
                "fromCache", fromCache
        ));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getPostsByCategory(@PathVariable String category) {
        long start = System.currentTimeMillis();

        List<Post> posts = postService.findByCategory(category);

        long end = System.currentTimeMillis();
        long duration = end-start;

        boolean fromCache = duration < 50;

        List<PostResponse> responses = posts.stream()
                .map(post -> PostResponse.from(post, fromCache))
                .toList();

        return ResponseEntity.ok(Map.of(
                "posts", responses,
                "count", responses.size(),
                "responseTime", duration + "ms",
                "fromCache", fromCache
        ));
    }

    /**
     * 게시글 수정
     *
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id,
                                                   @Valid @RequestBody PostRequest postRequest) {
        Post post = postService.update(id, postRequest);
        return ResponseEntity.ok(PostResponse.from(post));
    }

    /**
     * 게시글 생성
     *
     * @return
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest postRequest) {
        Post post = postService.create(postRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PostResponse.from(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test-cache-stampede/{id}")
    public ResponseEntity<Map<String, Object>> testCacheStampede(
            @PathVariable Long id,
            @RequestParam(defaultValue = "100") int threadCount
    ) throws InterruptedException {
        log.info("========================================");
        log.info("Cache Stampede 테스트 시작: id={}, threadCount={}", id, threadCount);
        log.info("========================================");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long startTime = System.currentTimeMillis();

        // 동시 요청 시작
        for (int i = 0; i < threadCount; i++) {
            final int requestNum = i + 1;
            executor.submit(() -> {
                try {
                    latch.countDown(); // 모든 스레드가 준비될 때까지 대기
                    latch.await();     // 동시 시작!

                    postService.findByIdWithLoadingCache(id);
                    log.debug("요청 {} 완료", requestNum);
                } catch (Exception e) {
                    log.error("요청 {} 실패", requestNum, e);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long duration = System.currentTimeMillis() - startTime;

        log.info("========================================");
        log.info("Cache Stampede 테스트 완료!");
        log.info("- 총 요청 수: {}", threadCount);
        log.info("- 소요 시간: {}ms", duration);
        log.info("========================================");

        return ResponseEntity.ok(Map.of(
                "message", "Cache Stampede 테스트 완료",
                "id", id,
                "threadCount", threadCount,
                "duration", duration + "ms"
        ));
    }



}

















