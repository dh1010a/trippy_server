package com.example.server.domain.comment.dto;

import com.example.server.domain.member.model.Scope;
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
        private Scope status;
    }

    @Data
    @Builder
    public static class CommentUpdateRequest{
        private Long commentId;
        private String content;
        private String memberId;
    }
}
