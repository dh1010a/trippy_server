package com.example.server.domain.recommend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public class RecommendResponseDto {

    @Data
    @Builder
    public static class RecommendPlaceResponseDto {
        private String title;
        private String hubTatsNm;
        private List<PlaceImageDto> imgUrl;
        private String content;
        private int imgCnt;

    }

    @Data
    @Builder
    public static class PlaceImageDto {
        private String imgUrl;
        private String thumbnailUrl;
        private int width;
        private int height;
        private String displaySiteName;
        private String docUrl;

    }
}
