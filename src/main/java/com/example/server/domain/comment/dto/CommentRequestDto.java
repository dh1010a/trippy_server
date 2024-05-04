package com.example.server.domain.comment.dto;

import lombok.Builder;
import lombok.Data;

public class CommentRequestDto {

    @Data
    @Builder
    public static class CommentBasicRequest{
        private Long postId;
        private String memberId;
        private Long parentId;
        private String content;
    }
}
