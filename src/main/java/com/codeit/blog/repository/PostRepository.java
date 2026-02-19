package com.codeit.blog.repository;

import com.codeit.blog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 카테고리별 게시글 조회
     */
    List<Post> findByCategory(String category);

    /**
     * 조회수 기준 인기 게시글 조회
     */
    List<Post> findTop10ByOrderByViewCountDesc();

    /**
     * 좋아요 수 기준 인기 게시글 조회
     */
    List<Post> findTop10ByOrderByLikeCountDesc();

    /**
     * 카테고리별 게시글 수 조회
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.category = :category")
    long countByCategory(@Param("category") String category);

    /**
     * 제목 검색
     */
    List<Post> findByTitleContaining(String keyword);


}
