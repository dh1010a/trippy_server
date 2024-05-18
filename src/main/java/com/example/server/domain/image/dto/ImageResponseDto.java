package com.example.server.domain.image.dto;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.model.PostType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public class ImageResponseDto {

    @Builder
    @Data
    public static class ImageBasicResponseDto{
        private Long id;
        private Long postId;
        private String imgUrl;
    }
}
