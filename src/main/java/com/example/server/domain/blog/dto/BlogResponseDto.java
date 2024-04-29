package com.example.server.domain.blog.dto;

import lombok.Builder;
import lombok.Data;

public class BlogResponseDto {

    @Data
    @Builder
    public static class IsDuplicatedDto {
        private String message;
        private boolean isDuplicated;
    }

    @Data
    @Builder
    public static class CreateBlogResponseDto {
        private long id;
        private String name;
        private boolean isSuccess;
    }

}
