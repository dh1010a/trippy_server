package com.example.server.domain.comment.dto;

import com.example.server.domain.comment.domain.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentDtoConverter {

    public static CommentResponseDto.CommentBasicResponse convertToCommentBasicResponse(Comment comment){
        CommentResponseDto.CommentBasicResponse parentResponse = null;
        if(comment.getParent()!=null) {
            parentResponse = convertToCommentBasicResponse(comment.getParent());
        }

        // 대댓글 entity -> basic Response
        List<CommentResponseDto.CommentBasicResponse> childCommentResponses = new ArrayList<>();
        if(comment.getChildComments() != null) {
            for (Comment childComment : comment.getChildComments()) {
                CommentResponseDto.CommentBasicResponse childCommentResponse = convertToCommentBasicResponse(childComment);
                childCommentResponses.add(childCommentResponse);
            }
        }

        return CommentResponseDto.CommentBasicResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .memberId(comment.getMember().getIdx())
                .parentComment(parentResponse)
                .childComments(childCommentResponses).build();

    }
}
