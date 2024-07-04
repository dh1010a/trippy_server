package com.example.server.domain.comment.dto;

import com.example.server.domain.comment.model.CommentStatus;
import com.example.server.domain.comment.model.DeleteStatus;
import com.example.server.domain.member.model.Scope;
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
        private String memberId;
        private Scope status;
        // 부모 댓글 id
        private ParentAndChildCommentResDto parentComment;
        // 자식 댓글들
        private List<ParentAndChildCommentResDto> childComments;
    }

    @Data
    @Builder
    public static class ParentAndChildCommentResDto {
        private Long id;
        private String content;
        private Scope status;
        private String memberId;
    }

    @Data
    @Builder
    public static class CommentTreeDTO {
        private Long id;
        private Long parentId;
        private String memberId;
        private String content;
        private Scope status;
        private int depth;
        private List<CommentTreeDTO> children;
    }
}
