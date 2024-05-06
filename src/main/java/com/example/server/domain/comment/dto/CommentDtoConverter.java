package com.example.server.domain.comment.dto;

import com.example.server.domain.comment.domain.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentDtoConverter {

    public static CommentResponseDto.CommentBasicResponse convertToCommentBasicResponse(Comment comment){
        CommentResponseDto.ParentAndChildCommentResDto parentResponse = null;
        if(comment.getParent()!=null) {
            parentResponse = convertToParentAndChildCommentRes(comment.getParent());
        }

        // 대댓글 entity -> basic Response
        List<CommentResponseDto.ParentAndChildCommentResDto> childCommentResponses = new ArrayList<>();
        if(comment.getChildComments() != null) {
            for (Comment childComment : comment.getChildComments()) {
                CommentResponseDto.ParentAndChildCommentResDto childCommentResponse = convertToParentAndChildCommentRes(childComment);
                childCommentResponses.add(childCommentResponse);
            }
        }

        return CommentResponseDto.CommentBasicResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .postId(comment.getPost().getId())
                .memberId(comment.getMember().getIdx())
                .parentComment(parentResponse)
                .childComments(childCommentResponses).build();

    }

    public static CommentResponseDto.ParentAndChildCommentResDto convertToParentAndChildCommentRes(Comment comment){
        return CommentResponseDto.ParentAndChildCommentResDto.builder()
                .content(comment.getContent())
                .id(comment.getId())
                .memberId(comment.getMember().getIdx())
                .build();

    }
}
