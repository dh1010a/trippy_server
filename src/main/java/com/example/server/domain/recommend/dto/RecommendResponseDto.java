package com.example.server.domain.recommend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public class RecommendResponseDto {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenResponseDto {
        @JsonProperty("response")
        private OpenImageResponseDto response;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenImageResponseDto {
        @JsonProperty("body")
        private Body body;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JsonProperty("items")
        private PlaceImageResponseDto items;

        @JsonProperty("numOfRows")
        private int numOfRows;
    }

    @Data
    @Builder
    public static class RecommendPlaceResponseDto {
        private String title;
        private String hubTatsNm;
        private List<PlaceImageDto> imgList;
        private String content;
        private int imgCnt;

    }



    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlaceImageResponseDto {
        @JsonProperty("item")
        private List<PlaceImageDto> item;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlaceImageDto {
        @JsonProperty("galTitle")
        private String galTitle;

        @JsonProperty("galWebImageUrl")
        private String galWebImageUrl;
    }
}
