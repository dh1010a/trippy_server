package com.example.server.domain.comment.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public class CommentResponseDto {

    @Data
    @Builder
    public static class CommentBasicResponse{
        private Long id;
        private String content;
        private Long postId;
        private Long memberId;
        // 부모 댓글 id
        private CommentBasicResponse parentComment;
        // 자식 댓글들
        private List<CommentBasicResponse> childComments;
    }
}
