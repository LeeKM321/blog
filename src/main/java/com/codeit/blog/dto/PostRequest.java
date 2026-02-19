package com.codeit.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 게시글 생성/수정 요청 DTO (Record 사용)
 */
public record PostRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        @Size(min = 10, message = "내용은 최소 10자 이상이어야 합니다")
        String content,

        @NotBlank(message = "작성자는 필수입니다")
        @Size(max = 100, message = "작성자는 100자를 초과할 수 없습니다")
        String author,

        @NotBlank(message = "카테고리는 필수입니다")
        @Size(max = 50, message = "카테고리는 50자를 초과할 수 없습니다")
        String category
) {
}
