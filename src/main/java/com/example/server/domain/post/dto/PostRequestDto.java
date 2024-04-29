package com.example.server.domain.post.dto;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.model.PostType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public class PostRequestDto {


    @Builder
    @Data
    public static class UploadPostRequestDto{
        private String email;
        private String title;
        private String body;
        private PostType postType;
        private String location;
        private List<String> images;
        private List<String> tags;
    }

    public static class UploadTagRequestDto{
        private String name;
        private Post post;
    }
}
