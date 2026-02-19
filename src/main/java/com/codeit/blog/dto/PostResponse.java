package com.codeit.blog.dto;

import com.codeit.blog.entity.Post;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 게시글 응답 DTO (Record 사용)
 */
public record PostResponse(
        Long id,
        String title,
        String content,
        String author,
        String category,
        Integer viewCount,
        Integer likeCount,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,
        Boolean fromCache
) {

    /**
     * Entity to DTO 변환 (캐시 여부 포함)
     */
    public static PostResponse from(Post post, boolean fromCache) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor(),
                post.getCategory(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                fromCache
        );
    }

    /**
     * Entity to DTO 변환 (캐시 여부 없음)
     */
    public static PostResponse from(Post post) {
        return from(post, false);
    }
}