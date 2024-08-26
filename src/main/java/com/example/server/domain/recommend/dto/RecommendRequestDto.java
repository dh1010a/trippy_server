package com.example.server.domain.recommend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public class RecommendRequestDto {

    @Builder
    @Data
    public static class GetRecommendRequest{
        private List<PostContentDto> likePostContentDtoList;
        private List<String> currentSearchList;
        private List<String> popularSearchList;
    }

    @Builder
    @Data
    public static class PostContentDto{
        private String title;
        private String body;
    }



}

