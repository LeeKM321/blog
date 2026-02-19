package com.codeit.blog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 블로그 게시글 엔티티
 */
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_view_count", columnList = "viewCount"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 생성자
    public Post(String title, String content, String author, String category) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.category = category;
        this.viewCount = 0;
        this.likeCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 로직
    public void update(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementLikeCount() {
        this.likeCount++;
        this.updatedAt = LocalDateTime.now();
    }
}