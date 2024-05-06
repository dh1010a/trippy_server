package com.example.server.domain.post.dto;

import lombok.Builder;
import lombok.Data;

public class LikeResponseDto {

    @Builder
    @Data
    public static class LikeBasicResponseDto{
        private Long likeId;
        private Long postId;
        private String memberId;
        private Integer likeCount;
    }
}
