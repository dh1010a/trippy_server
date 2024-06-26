package com.example.server.domain.post.dto;

import lombok.Builder;
import lombok.Data;

public class TagResponseDto {
    @Builder
    @Data
    public static class TagBasicResponseDto{
        private Long id;
        private Long postId;
        private String name;
    }


}
