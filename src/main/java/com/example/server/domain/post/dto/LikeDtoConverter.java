package com.example.server.domain.post.dto;

import com.example.server.domain.post.domain.Like;

public class LikeDtoConverter {
    public static LikeResponseDto.LikeBasicResponseDto convertToLikeBasicDto(Like like, Integer likeCount){
        return LikeResponseDto.LikeBasicResponseDto.builder()
                .likeId(like.getId())
                .postId(like.getPost().getId())
                .likeCount(likeCount)
                .memberId(like.getMember().getMemberId())
                .build();
    }
}
