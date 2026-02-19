package com.codeit.blog.service;

import com.codeit.blog.entity.Post;
import com.codeit.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;

    /**
     * 게시글 단건 조회
     */
    public Post findById(Long id) {
        log.debug("DB에서 게시글 조회: id={}", id);
        simulateSlowQuery();
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
    }

    /**
     * 카테고리별 게시글 조회
     */
    public List<Post> findByCategory(String category) {
        log.debug("DB에서 카테고리별 게시글 조회: category={}", category);
        simulateSlowQuery();
        return postRepository.findByCategory(category);
    }

    /**
     * 인기 게시글 조회 (조회수 기준)
     */
    public List<Post> findPopularPosts() {
        log.debug("DB에서 인기 게시글 조회");
        simulateSlowQuery();
        return postRepository.findTop10ByOrderByViewCountDesc();
    }

    /**
     * 모든 게시글 조회
     */
    public List<Post> findAll() {
        log.debug("DB에서 모든 게시글 조회");
        return postRepository.findAll();
    }

    /**
     * 게시글 생성
     */
    @Transactional
    public Post create(PostRequest request) {
        log.info("게시글 생성: title={}", request.title());

        Post post = new Post(
                request.title(),
                request.content(),
                request.author(),
                request.category()
        );

        return postRepository.save(post);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public Post update(Long id, PostRequest request) {
        log.info("게시글 수정: id={}", id);

        Post post = findById(id);
        post.update(request.title(), request.content(), request.category());

        return post;
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void delete(Long id) {
        log.info("게시글 삭제: id={}", id);
        postRepository.deleteById(id);
    }

    /**
     * 모든 캐시 초기화
     */
    @Transactional
    public void evictAllCaches() {
        log.info("모든 캐시 초기화");
    }

    /**
     * 조회수 증가
     */
    @Transactional
    public void incrementViewCount(Long id) {
        Post post = findById(id);
        post.incrementViewCount();
    }

    /**
     * 좋아요 증가
     */
    @Transactional
    public void incrementLikeCount(Long id) {
        Post post = findById(id);
        post.incrementLikeCount();
    }

    /**
     * DB 조회 지연 시뮬레이션 (1초)
     */
    private void simulateSlowQuery() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted", e);
        }
    }

}