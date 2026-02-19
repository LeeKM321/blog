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


}

















