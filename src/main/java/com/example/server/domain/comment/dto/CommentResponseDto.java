package com.example.server.domain.comment.dto;

import com.example.server.domain.comment.model.CommentStatus;
import com.example.server.domain.comment.model.DeleteStatus;
import com.example.server.domain.member.model.Scope;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponseDto {

    @Data
    @Builder
    public static class CommentBasicResponse{
        private Long id;
        private String content;
        private Long postId;
        private String mentionMemberId;
        private String mentionMemberNickName;
        private Long mentionCommentId;
        private Scope status;
        private LocalDateTime createDateTime;
        private CommentMemberDto member;
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
        private LocalDateTime createDateTime;
        private CommentMemberDto member;
    }

    @Data
    @Builder
    public static class CommentTreeDTO {
        private Long id;
        private Long parentId;
        private String mentionMemberId;
        private String mentionMemberNickName;
        private Long mentionCommentId;
        private String content;
        private Scope status;
        private int depth;
        private LocalDateTime createDateTime;
        private CommentMemberDto member;
        private List<CommentTreeDTO> children;
    }

    @Data
    @Builder
    public static class CommentMemberDto{
        private String memberId;
        private String nickName;
        private String profileUrl;

    }
}
