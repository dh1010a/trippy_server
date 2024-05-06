package com.example.server.domain.comment.dto;

import com.example.server.domain.comment.domain.Comment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
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
                    .status(comment.getStatus())
                .parentComment(parentResponse)
                .childComments(childCommentResponses).build();

    }

    public static CommentResponseDto.ParentAndChildCommentResDto convertToParentAndChildCommentRes(Comment comment){
        return CommentResponseDto.ParentAndChildCommentResDto.builder()
                .content(comment.getContent())
                .id(comment.getId())
                .memberId(comment.getMember().getMemberId())
                .status(comment.getStatus())
                .build();

    }

    public static CommentResponseDto.CommentTreeDTO converToCommentTreeDTO(Comment comment){
        Long parentId = 0L;
        if(comment.getParent()!=null) {
            parentId = comment.getParent().getId();
        }
        List<CommentResponseDto.CommentTreeDTO> childList = new ArrayList<>();
        if(!comment.getChildComments().isEmpty()) {
            for (Comment childComment : comment.getChildComments()) {
                childList.add(converToCommentTreeDTO(childComment));
            }
        }
        return CommentResponseDto.CommentTreeDTO.builder()
                .parentId(parentId)
                .depth(getDepth(comment))
                .id(comment.getId())
                .children(childList)
                .memberId(comment.getMember().getMemberId())
                .status(comment.getStatus())
                .content(comment.getContent()).build();
    }

    public static Map<Long, CommentResponseDto.CommentTreeDTO>  convertToTreeDtoMap(List<Comment> comments) {
        Map<Long, CommentResponseDto.CommentTreeDTO> map = new HashMap<>();

        for (Comment comment : comments) {
            CommentResponseDto.CommentTreeDTO commentDTO = converToCommentTreeDTO(comment);

            // 최상위 댓글
            if (comment.getParent() == null) {
                commentDTO.setChildren(new ArrayList<>());
                map.put(comment.getId(), commentDTO);
            } else {
                // 부모 댓글이 있는 경우
                Comment parentComment = comment.getParent();
                if (map.containsKey(parentComment.getId())) {
                    map.get(parentComment.getId()).getChildren().add(commentDTO);
                }

            }
        }

        return map;

    }

    private static int getDepth(Comment comment) {
        int depth = 1;
        Comment parent = comment.getParent();
        while (parent != null) {
            depth++;
            parent = parent.getParent();
        }
        return depth;
    }

}
