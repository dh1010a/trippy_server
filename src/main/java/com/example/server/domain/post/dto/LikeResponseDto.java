package com.example.server.domain.post.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public class LikeResponseDto {

    @Builder
    @Data
    public static class LikeBasicResponseDto{
        private Long likeId;
        private Long postId;
        private String memberId;
        private String nickName;
        private Integer likeCount;
    }

    @Builder
    @Data
    public static class LikeListBasicResponseDto{
        private int likeCount;
        private long postId;
        private List<LikeListDto> likeList;
    }

    @Builder
    @Data
    public static class LikeListDto {
        private Long likeId;
        private String memberId;
        private String nickName;
    }

}
