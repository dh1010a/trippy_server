package com.example.server.domain.post.dto;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.model.PostType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public class PostResponseDto {

    @Builder
    @Data
    public static class UploadPostResultResponseDto{
        private Long idx;
        private String email;
        private String title;
        private String body;
        private PostType postType;
        private String location;
        private List<Image> images;
        private List<Tag> tags;
        private Boolean isSuccess;
    }

}
