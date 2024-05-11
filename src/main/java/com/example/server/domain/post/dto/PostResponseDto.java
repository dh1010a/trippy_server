package com.example.server.domain.post.dto;

import com.example.server.domain.image.dto.ImageResponseDto;
import com.example.server.domain.post.model.PostType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public class PostResponseDto {

    @Builder
    @Data
    public static class PostBasicResponseDto {
        private Long id;
        private String email;
        private String title;
        private String body;
        private PostType postType;
        private String location;
        private List<ImageResponseDto.ImageBasicResponseDto> images;
        private List<TagResponseDto.TagBasicResponseDto> tags;
        private Boolean isSuccess;
    }

    @Builder
    @Data
    public static class DeletePostResultResponseDto{
        private Long id;
        private Boolean isSuccess;
    }

    @Builder
    @Data
    public static class GetPostResponseDto{
        private Long id;
        private String email;
        private String title;
        private String body;
        private PostType postType;
        private String location;
        private List<ImageResponseDto.ImageBasicResponseDto> images;
        private List<TagResponseDto.TagBasicResponseDto> tags;
        private Integer likeCount;
        private Boolean isSuccess;
    }


}
