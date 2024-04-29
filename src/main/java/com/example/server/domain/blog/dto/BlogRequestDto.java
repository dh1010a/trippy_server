package com.example.server.domain.blog.dto;

import lombok.Builder;
import lombok.Data;

public class BlogRequestDto {

    @Builder
    @Data
    public static class CreateBlogRequestDto {
        private String name;
        private String introduce;
    }
}
