package com.codeit.blog.service;

import com.codeit.blog.dto.PostRequest;
import com.codeit.blog.entity.Post;
import com.codeit.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(value = "posts", key = "#id")
    public Post findById(Long id) {
        log.debug("DB에서 게시글 조회: id={}", id);
        simulateSlowQuery();
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
    }

    /**
     * 카테고리별 게시글 조회
     */
    @Cacheable(value = "postsByCategory", key = "#category")
    public List<Post> findByCategory(String category) {
        log.debug("DB에서 카테고리별 게시글 조회: category={}", category);
        simulateSlowQuery();
        return postRepository.findByCategory(category);
    }

    /**
     * 인기 게시글 조회 (조회수 기준)
     * 인기 게시글 목록은 모든 사용자에게 동일하니까 하나만 캐시하면 됩니다.
     */
    @Cacheable(value = "popularPosts") //key를 따로 지정하지 않는다면 자동으로 'SimpleKey.EMPTY' 값으로 키를 자동 세팅합니다.
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
    @CachePut(value = "posts", key = "#result.id") // #result는 메서드 리턴값을 의미합니다.
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
    @CachePut(value = "posts", key = "#id")
    public Post update(Long id, PostRequest request) {
        log.info("게시글 수정: id={}", id);

        Post post = findById(id);
        post.update(request.title(), request.content(), request.category());

        return post; // 결과가 캐시에 반영
    }

    /**
     * 게시글 삭제
     * beforeInvocation = false(기본값): 메서드 실행 -> 성공하면 캐시 제거, 실패하면 캐시 유지
     * beforeInvocation = true: 캐시 먼저 제거 -> 메서드 실행, 실패해도 캐시는 이미 제거됨
     */
    @Transactional
    @CacheEvict(value = "posts", key = "#id")
    public void delete(Long id) {
        log.info("게시글 삭제: id={}", id);
        postRepository.deleteById(id);
    }

    /**
     * 모든 캐시 초기화
     */
    @Transactional
    @CacheEvict(value = {"posts", "postsByCategory", "popularPost"}, allEntries = true)
    public void evictAllCaches() {
        log.info("모든 캐시 초기화");
    }

    // condition을 이용해서 특정 조건에서만 캐싱
    // unless: 특정 조건에서는 캐싱을 하지 않겠다
    @Cacheable(value = "posts", key = "#id", condition = "#id <= 100", unless = "#result == null")
    public Post findByIdOrNull(Long id) {
        return postRepository.findById(id).orElse(null);
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