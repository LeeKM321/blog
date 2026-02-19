package com.codeit.blog.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PostServiceTest.class);
    @Autowired
    private PostService postService;

    @Test
    @DisplayName("Loading Cache 동작 테스트")
    void testLoadingCache() {
        ExecutorService executor = Executors.newFixedThreadPool(100);

        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                postService.findByIdWithLoadingCache(2543L);
            });
        }

    }


}










